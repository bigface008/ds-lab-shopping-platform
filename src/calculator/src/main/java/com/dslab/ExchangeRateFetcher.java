package com.dslab;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRateFetcher implements Watcher, AsyncCallback.DataCallback {
    private ZooKeeper zk;
    private String znode;
    private Map<String, BigDecimal> rate;

    public ExchangeRateFetcher(String zkUrl, String znode) throws IOException {
        zk = new ZooKeeper(zkUrl, 3000, this);
        this.znode = znode + "/";
        rate = new HashMap<>();
    }

    public void close() throws InterruptedException {
        zk.close();
    }

    BigDecimal getRate(String currency) {
        if (rate.get(znode + currency) == null) {
            zk.getData(znode + currency, true, this, null);

            // blocked until rate is got
            while (rate.get(znode + currency) == null) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
        return rate.get(znode + currency);
    }

    public void process(WatchedEvent event) {
        if (event.getType() != Event.EventType.None) {
            String path = event.getPath();
            if (path != null) {
                if (rate.containsKey(path)) {
                    System.out.println("[ExchangeRateFetcher] " + path + " updated!");
                    // get node data when it changed
                    zk.getData(path, true, this, null);
                }
            }
        }
    }

    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        switch (KeeperException.Code.get(rc)) {
            // update rate and wake up any thread blocked at getRate()
            case OK:
                System.out.println("[ExchangeRateFetcher] " + path + " fetch success!");
                rate.put(path, new BigDecimal(new String(data)));
                synchronized (this) {
                    notifyAll();
                }
                break;
            // retry get data
            case NONODE:
            case SESSIONEXPIRED:
            case NOAUTH:
            default:
                System.out.println("[ExchangeRateFetcher] " + path + " fetch fail, retry!");
                zk.getData(path, true, this, null);
        }
    }
}
