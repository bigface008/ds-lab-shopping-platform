package com.dslab;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class DistLock implements Watcher, AsyncCallback.StatCallback {

    private ZooKeeper zk;
    private String znode;

    boolean isDead() {
        return dead;
    }

    private boolean dead;
    private boolean waiting;
    private boolean lock;

    DistLock(ZooKeeper zk, String znode) {
        this.zk = zk;
        this.znode = znode;
        zk.exists(znode, true, this, null);
    }

    private void tryLock() {
        try {
            zk.create(znode, null, OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (Exception e) {
            return;
        }
        lock = true;
        waiting = false;
        synchronized (this) {
            notifyAll();
        }
    }

    boolean acquire() {
        waiting = true;
        tryLock();
        try {
            synchronized (this) {
                while (waiting && !dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Acquiring was interrupted!");
        }
        return lock;
    }

    void release() {
        if (lock) {
            try {
                zk.delete(znode, -1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock = false;
            }
        }
    }

    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() != Event.EventType.None) {
            if (path != null && path.equals(znode)) {
                zk.exists(znode, true, this, null);
            }
        }
    }

    public void processResult(int rc, String path, Object ctx, Stat stat) {
        switch (KeeperException.Code.get(rc)) {
            case NONODE:
                if (waiting) {
                    tryLock();
                }
            case OK:
                break;
            case SESSIONEXPIRED:
            case NOAUTH:
                dead = true;
                return;
            default:
                zk.exists(znode, true, this, null);
        }
    }
}
