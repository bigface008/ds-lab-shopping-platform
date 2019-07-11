import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import static org.apache.zookeeper.ZooDefs.Ids.CREATOR_ALL_ACL;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class DistLock {

    boolean lock;
    boolean wait;
    ZooKeeper zk;
    DataMonitor dm;
    String znode;
    public DistLock(ZooKeeper zk, DataMonitor dm, String znode)
    {
        lock = false;
        wait = false;
        this.zk = zk;
        this.dm = dm;
        this.znode = znode;
    }

    public boolean acquire()
    {
        try {
            synchronized (this) {
                this.wait = true;
                this.zk.exists(this.znode, true, this.dm, null);
                while (this.wait) {
                    wait();
                }
            }
        } catch (InterruptedException e)
        {
        }
        return this.lock;
    }

    public void release()
    {
        if (this.lock)
        {
            try {

                this.zk.delete(this.znode, -1);
            }
            catch (KeeperException e)
            {
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    public void tryLock()
    {
        if (wait == false)
        {
            return;
        }
        try {
            System.out.println(this.zk.create(this.znode, null, OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
        }
        catch (KeeperException e)
        {
            KeeperException.Code c = e.code();
            if (c == KeeperException.Code.NODEEXISTS)
            {
                this.zk.exists(this.znode, true, this.dm, null);
                return;
            }
            else
            {
                wait = false;
                return;
            }
        }
        catch (InterruptedException e)
        {
            wait = false;
            return;
        }
        lock = true;
        wait = false;
        synchronized (this) {
            notifyAll();
        }
        return;
    }
}
