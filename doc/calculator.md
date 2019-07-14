# Calculator

## 功能

从 Kafka 的一个 topic 中读取 new order 消息，将订单中货物数量和库存比对，无法提供的将拒绝订单，剩下的根据从 zookeeper 获取的汇率计算客户应该支付的数额。

## 实现思路

基于 Spark 平台的抽象分了 3 个 RDD

```
  Kafka                                            Zookeeper
   v ^                     commodity          commodity  |
|------|        |--------|   v ^     |------------|   v  v
|record|--map-->|newOrder|--filter-->|successOrder|--output->result
|------|   |    |--------|    |      |------------|          amount
           v                  v
        result              result
    (success:null)       (paid:null)
    (paid:null)
```

1. InputDStream 设置为 Kafka
2. record 为 Kafka 消息的 key, value 对, 这里的 value 为 newOrder
3. map 将 record -> newOrder, 并将 success 和 paid 为 null 的 result 存入数据库， 以便实现 exactly-once 语义
4. newOrder 为新订单，参照[数据结构](../README.md#12-data-structures)
5. filter 结合商品库存判断订单能否被满足，若可以则扣除库存，设置success:true入库，并将该订单传入 successOrder。否则设置 success:false, paid:0 入库后抛弃。
6. successOrder 为可以满足的订单，此时库存已经扣除。
7. output 从Zookeeper 获取汇率，从 commodity 获取价格，计算客户最终应付款，并将结果记入对应货币的成交总额。

## 构建

```
mvn package
mv target/calculator-*-jar-with-dependencies.jar ./calculator.jar
```

即可获得 `calculator.jar`

## 运行参数

1. /path/to/config.yml
2. /path/to/hibernate.cfg.xml

## 配置 [`config.json`](../config/calculator/config.json)

- kafka-url, 逗号隔开的kafka集群的host-port列表
- topic, 要消费的话题
- consumer-group, 消费组
- zookeeper-url, 连接zookeeper的host-port
- exchange-rate-znode, 存储汇率的znode

## 部署

### 编写脚本

- `build.sh`
   1. 从 github 拉取项目
   2. 创建 `calculator` 目录
   3. 将配置文件`config.json`, `hibernate.cfg.xml` 复制到该目录下
   4. 构建 `calculator.jar` 并放入该目录下

- `deploy.sh`
   1. 将 `calculator` 目录传到所有机器上
   2. 将 `start-calculator.sh` 脚本复制到 `ds-1` 上
   3. 运行 `start-calculator.sh`

- `start-calculator.sh`
   - 使用 `spark-submit` 将驱动程序 `calculator.jar` 提交到 Spark 集群中
   - 提交设置部署模式为集群，并将两个配置文件的路径作为参数传入驱动程序

### 部署驱动程序

由于 Spark 分布式的特性，我们的驱动程序会被打散在集群各个节点运行，因此配置文件的分配会比较困难，相较于配置复杂的远程文件系统，我们选择直接将配置和驱动程序拷贝到每个结点中(正如`deploy.sh`所做)。

1. 构建项目

   ```
   ./build.sh
   ```

2. 修改配置文件，填上合适的参数

3. 将它们部署到集群中

   ```
   ./deploy.sh
   ```

### 验证部署

部署完成后可以在 spark 集群 master 的 web ui 看到

> Running Applications (1)
> Running Drivers (1)

当前集群中正在运行一个驱动程序和一个应用程序(Java Stream 上下文)。

现在运行 sender.py 应该可以看到订单被正确地处理了。
