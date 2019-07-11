/**
 * A simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 */
import java.io.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

import org.apache.zookeeper.*;
import org.yaml.snakeyaml.Yaml;

import static java.lang.Thread.sleep;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class Executor
        implements Watcher, Runnable
{
    String znode;
    DataMonitor dm;
    ZooKeeper zk;
    BigDecimal init_exchange_rate;
    int period;

    public Executor(String hostPort, String znode, BigDecimal init_exchange_rate, int period) throws IOException {
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode + "/lock", null);
        this.init_exchange_rate = init_exchange_rate;
        this.znode = znode;
        this.period = period;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("USAGE: Executor hostPort znode exchange_rate.yml currency period(s) [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String cfg = args[2];
        String currency = args[3];
        String period = args[4];
        FileInputStream is;
        Map<String, String> m;
        try {
            is = new FileInputStream(cfg);
            Yaml yml = new Yaml();
            m = yml.load(is);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        BigDecimal ier = new BigDecimal(m.get(currency));
        try {
            new Executor(hostPort, znode, ier, Integer.parseInt(period) * 1000).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***************************************************************************
     * We do process any events ourselves, we just need to forward them on.
     */
    public void process(WatchedEvent event) {
        dm.process(event);
    }

    public void run() {
        try {
            this.zk.create(this.znode, null, OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            byte[] b = init_exchange_rate.toString().getBytes();
            this.zk.setData(this.znode, b, -1);
        } catch (KeeperException e) {
            KeeperException.Code c = e.code();
            if (c != KeeperException.Code.NODEEXISTS)
            {
                e.printStackTrace();
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (dm.lock.acquire())
        {
            while (!dm.dead) {
                Random rand = new Random();
                BigDecimal inc = new BigDecimal(rand.nextInt(101) - 50).divide(new BigDecimal(500), 5, BigDecimal.ROUND_HALF_EVEN);
                inc = inc.add(new BigDecimal(1));
                BigDecimal exchange_rate = init_exchange_rate.multiply(inc).setScale(5, BigDecimal.ROUND_HALF_EVEN);

                byte[] arr = exchange_rate.toString().getBytes();
                try {
                    this.zk.setData(this.znode, arr, -1);
                }
                catch (KeeperException e)
                {
                    e.printStackTrace();
                    break;
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    break;
                }
                try {
                    sleep(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            System.err.println("OOOOOOOH SHIT!");
        }
    }
}
