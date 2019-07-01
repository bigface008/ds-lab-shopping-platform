# Zookeeper 集群

## 部署

我们接下来在 `ds-1`, `ds-2`, `ds-3` 三个虚拟机上部署 zookeeper 实例，组成高可用的集群。

Ubuntu 16.06 的源中包含了 zookeeperd 3.4.8，直接安装即可。

1. 登录`ds-1`
   ```bash
   $ ssh ubuntu@{platform-ip} -p 30xx1
   ```

2. 安装zookeeper
   ```bash
   $ sudo apt install zookeeperd
   ```

3. 编辑配置文件[`/etc/zookeeper/conf/zoo.cfg`](../config/zookeeper/zoo.cfg)，指定client连接端口，以及集群间通信端口，其他配置采用默认值：
   ```
   clientPort={client port}
   server.1=ds-1:{port 1}:{port 2}
   server.2=ds-2:{port 1}:{port 2}
   server.3=ds-3:{port 1}:{port 2}
   ```

4. 将虚拟机序号 `1` 写入 `/etc/zookeeper/conf/myid`
   ```bash
   $ sudo bash -c "echo 1 > /etc/zookeeper/conf/myid"
   ```

5. 重启zookeeper服务
   ```bash
   sudo service zookeeper restart
   ```

最后依次登陆 `ds-2`, `ds-3` 执行以上步骤，注意第4步写入ds-{N}对应的序号{N}。

## 验证是否部署成功

1. 登陆任意一台虚拟机
   ```bash
   $ ssh ubuntu@{platform-ip} -p 30xx{N}
   ```

2. 依次查看每个实例的状态
   ```bash
   $ echo stat | nc ds-{N} {client port}
   ...
   Mode: follower 或 leader
   ...
   ```

   应该可以看到其中一个是 **leader**，其余为 **follower**
