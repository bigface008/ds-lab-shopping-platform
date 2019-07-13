package com.dslab;

import org.apache.zookeeper.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

import static java.lang.Thread.sleep;
import static org.apache.zookeeper.Watcher.Event.KeeperState.Expired;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class Updater implements Watcher, Runnable {

    private ZooKeeper zk;
    private DistLock lock;
    private String znode;
    private boolean dead;
    private BigDecimal initial;
    private int period;

    private Updater(UpdaterConfig config) throws IOException {
        znode = config.getZnode() + "/" + config.getCurrency();
        zk = new ZooKeeper(config.getUrl(), 3000, this);
        lock = new DistLock(zk,  znode + "/lock");
        initial = new BigDecimal(config.getInitial().get(config.getCurrency()));
        period = config.getPeriod() * 1000;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("USAGE: updater config.yml");
            System.exit(2);
        }

        UpdaterConfig config;
        FileInputStream is;
        try {
            is = new FileInputStream(args[0]);
            config = new Yaml(new Constructor(UpdaterConfig.class)).load(is);
            is.close();
            new Updater(config).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None) {
            if (event.getState() == Expired) {
                dead = true;
            }
        } else {
            lock.process(event);
        }
    }

    public void run() {
        try {
            zk.create(znode, null, OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            byte[] b = initial.toString().getBytes();
            zk.setData(znode, b, -1);
        } catch (KeeperException e) {
            if (e.code() != KeeperException.Code.NODEEXISTS) {
                e.printStackTrace();
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Waiting for other instance releasing lock.");
        if (lock.acquire()) {
            System.out.println("Start updating exchange rate.");

            while (!lock.isDead() && !dead) {
                Random r = new Random();
                BigDecimal inc = new BigDecimal(r.nextInt(101) - 50).divide(new BigDecimal(500), 5, BigDecimal.ROUND_HALF_EVEN);
                inc = inc.add(new BigDecimal(1));
                BigDecimal exchange_rate = initial.multiply(inc).setScale(5, BigDecimal.ROUND_HALF_EVEN);
                System.out.println(exchange_rate.toString());

                byte[] bytes = exchange_rate.toString().getBytes();
                try {
                    zk.setData(znode, bytes, -1);
                    sleep(period);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
