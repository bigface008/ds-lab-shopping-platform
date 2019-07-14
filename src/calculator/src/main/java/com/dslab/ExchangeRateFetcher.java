package com.dslab;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRateFetcher {
    private Map<String, ExchangeRate> rates;
    private String zkUrl;
    private String znode;

    public ExchangeRateFetcher(String zkUrl, String znode) {
        rates = new HashMap<>();
        this.zkUrl = zkUrl;
        this.znode = znode;
    }

    public BigDecimal getRate(String currency) throws IOException {
        ExchangeRate rate = rates.get(currency);
        if (rate == null) {
            rate = new ExchangeRate(zkUrl, znode, currency);
            rates.put(currency, rate);
        }
        return rate.getRate();
    }

    public void close() throws InterruptedException {
        for (ExchangeRate rate: rates.values()) {
            rate.close();
        }
    }

}

class ExchangeRate implements Watcher, AsyncCallback.DataCallback {
    private ZooKeeper zk;
    private String znode;
    private BigDecimal rate;

    ExchangeRate(String zkUrl, String znode, String currency) throws IOException {
        zk = new ZooKeeper(zkUrl, 3000, this);
        this.znode = znode + "/" + currency;
        zk.getData(this.znode, true, this, null);
    }

    public void close() throws InterruptedException {
        zk.close();
    }

    BigDecimal getRate() {
        while (rate == null) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        return rate;
    }

    public void process(WatchedEvent event) {
        if (event.getType() != Event.EventType.None) {
            String path = event.getPath();
            if (path != null && path.equals(znode)) {
                System.out.println("[ExchangeRateFetcher] Znode updated!");
                zk.getData(this.znode, true, this, null);
            }
        }
    }

    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        switch (KeeperException.Code.get(rc)) {
            case OK:
                System.out.println("[ExchangeRateFetcher] fetch success!");
                rate = new BigDecimal(new String(data));
                synchronized (this) {
                    notifyAll();
                }
                break;
            case NONODE:
            case SESSIONEXPIRED:
            case NOAUTH:
                System.out.println("[ExchangeRateFetcher] fetch fail!");
                rate = null;
            default:
                System.out.println("[ExchangeRateFetcher] fetch retry!");
                zk.getData(this.znode, true, this, null);
        }
    }
}
