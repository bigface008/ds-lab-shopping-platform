# Kafka 部署流程

## 1 编写部署脚本

[脚本目录如下](../script/kafka)

```
/
|- kafka/
|  |- deploy.sh         # 在远端服务器中执行的部署脚本
|  |- kafka.properties  # kafka 配置文件
|  |- kafka.service     # systemd 配置文件
|
|- deploy-remote.sh     # 部署脚本
```

### 1.1 [`deploy-remote.sh`](../script/kafka/deploy-remote.sh)

接收一个参数作为要部署的远端服务器的机器编号 ( 1 ~ 4 )

它将目录 `kafka` 上传到指定服务器上后执行其中的 `deploy.sh` 脚本

### 1.2 [`kafka/deploy.sh`](../script/kafka/kafka/deploy.sh)

接收一个参数作为 broker 编号 ( 1 ~ 4 )

从 `http://mirror.bit.edu.cn/apache/kafka/2.2.0/kafka_2.12-2.2.0.tgz` 下载程序包并解压。

将配置文件中的 `broker.id` 改为接收到的参数并放入 kafka 的 config 目录下，然后创建日志目录，接着再将整个 kafka 目录移动到 `/opt/kafka`。

将 systemd 配置文件 `kafka.service` 移动到 `/usr/lib/systemd/system`，方便用 systemd 来管理我们的 kafka 服务。

最后启动 kafka 服务并设置成开机启动

### 1.3 [`kafka/kafka.service`](../script/kafka/kafka/kafka.service)

启动脚本为 `/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/cluster.properties`

停止脚本为 `/opt/kafka/bin/kafka-server-stop.sh`

设置为在 failure 时自动重启服务

### 1.4 [`kafka/kafka.properties`](../script/kafka/kafka/kafka.properties)

- `broker.id` 会被部署脚本设置成指定的 broker 序号
- `log.dirs` 设置为 `/opt/kafka/log` 是 broker 的日志存储目录
- `listeners` 为监听地址
- `zookeeper.connect` 为我们的 zookeeper 集群的地址:端口

## 2 部署

写好脚本之后只要运行就可以了，在本地执行

```
$ ./deploy-remote.sh 1
$ ./deploy-remote.sh 2
$ ./deploy-remote.sh 3
$ ./deploy-remote.sh 4
```

在 `ds-1`, `ds-2`, `ds-3`, `ds-4` 上分别部署了 broker 1, 2, 3, 4

## 3 验证

登录 `ds-1`，通过下述两种方式验证部署

1. 通过 zookeeper 包中的命令行客户端，可以查看 `/brokers/ids` 下的子节点，应该有 `[1, 2, 3, 4]` 即成功在全部 4 台机器上部署了 Kafka broker

   ```
   $ ./zkCli.sh
   ...
   [zk: {zk-host}:{port}(CONNECTED) 0] ls /brokers/ids
   [1, 2, 3, 4]
   [zk: {zk-host}:{port}(CONNECTED) 1]
   ```

2. 用 kafka 包中的 topic 管理工具创建一个话题 `test-topic`, 指定复制因数为 3, 分区数为 5

   ```
   $ /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server {kafka-host}:{port} --replication-factor 3 --partitions 5 --topic test-topic
   ```

   查看刚刚创建的话题

   ```
   $ /opt/kafka/bin/kafka-topics.sh --describe --bootstrap-server {kafka-host}:{port} --topic test-topic
   Topic:test-topic        PartitionCount:5        ReplicationFactor:3     Configs:min.insync.replicas=2,segment.bytes=1073741824
        Topic: test-topic       Partition: 0    Leader: 4       Replicas: 4,2,3 Isr: 4,2,3
        Topic: test-topic       Partition: 1    Leader: 2       Replicas: 2,3,1 Isr: 2,3,1
        Topic: test-topic       Partition: 2    Leader: 3       Replicas: 3,1,4 Isr: 3,1,4
        Topic: test-topic       Partition: 3    Leader: 1       Replicas: 1,4,2 Isr: 1,4,2
        Topic: test-topic       Partition: 4    Leader: 4       Replicas: 4,3,1 Isr: 4,3,1
   ```

   可以看到该消息有5个分区, 每个分区有三个复制。

   - 分区 0 在 broker 4, 3, 2 上各有一份复制, 其中 broker 4 为 leader, 已完成数据同步的有 broker 4, 2, 3
   - ...

   在 `ds-3` 上使用命令 `systemctl stop kafka` 停止该 broker, 再次查看话题。

   ```
   $ /opt/kafka/bin/kafka-topics.sh --describe --bootstrap-server {kafka-host}:{port} --topic test-topic
   Topic:test-topic        PartitionCount:5        ReplicationFactor:3     Configs:min.insync.replicas=2,segment.bytes=1073741824
        Topic: test-topic       Partition: 0    Leader: 4       Replicas: 4,2,3 Isr: 4,2
        Topic: test-topic       Partition: 1    Leader: 2       Replicas: 2,3,1 Isr: 2,1
        Topic: test-topic       Partition: 2    Leader: 1       Replicas: 3,1,4 Isr: 1,4
        Topic: test-topic       Partition: 3    Leader: 1       Replicas: 1,4,2 Isr: 1,4,2
        Topic: test-topic       Partition: 4    Leader: 4       Replicas: 4,3,1 Isr: 4,1
   ```

   此时 broker 3 从所有参与的分区上被移出了已同步的集合。

   在 `ds-3` 上使用命令 `systemctl start kafka` 重新启动 broker, 再次查看话题。

   ```
   $ /opt/kafka/bin/kafka-topics.sh --describe --bootstrap-server {kafka-host}:{port} --topic test-topic
   Topic:test-topic        PartitionCount:5        ReplicationFactor:3     Configs:min.insync.re   plicas=2,segment.bytes=1073741824
        Topic: test-topic       Partition: 0    Leader: 4       Replicas: 4,2,3 Isr: 4,2,3
        Topic: test-topic       Partition: 1    Leader: 2       Replicas: 2,3,1 Isr: 2,1,3
        Topic: test-topic       Partition: 2    Leader: 1       Replicas: 3,1,4 Isr: 1,4,3
        Topic: test-topic       Partition: 3    Leader: 1       Replicas: 1,4,2 Isr: 1,4,2
        Topic: test-topic       Partition: 4    Leader: 4       Replicas: 4,3,1 Isr: 4,1,3
   ```

   此时 broker 3 又重新加入了这些分区。

   可见 Kafka 会自动为分区选择合适的分布，而且出现单点故障时仍然可以正常工作，在故障的机器恢复后也能自动重新加入复制，真是妙哉。
