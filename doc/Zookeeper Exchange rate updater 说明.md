# Zookeeper Exchange rate updater 说明

516030910459 邵欣阳

## 1. 实现思路

* 首先，我们有一个datamonitor，可以监视zookeeper的znode的状态变化，当被监视的znode的状态发生变化的时候，可以触发它的动作。
* 然后，我们使用zookeeper的临时节点的特性，创建了分布式锁的机制。需要拿锁的线程，在需要上锁的节点下创建临时节点，其它节点便无法执行创建操作，这个节点即被锁上了。
* 我们的进程被创建后，首先试图创建对应货币汇率的节点，如果创建成功，则从配置文件中读取原始汇率，写入znode，如果失败，则说明该节点已经被创建，就简单地跳过这一步骤。
* 我们的进程，每隔一定时间，尝试更新一次znode的数据，即更新汇率。更新方法是，读取znode数据，生成一个随机变化量，用这个量更新数据，将数据写回znode。

## 2. 实际测试

* 最初的测试，汇率很容易更新成负数。通过限制它的更新范围，它不会再变成离谱的数据。
* 锁的工作符合预期。
* 机箱的风扇转得很快。
* 一切正常。

## 3. 使用方式

* 使用`maven`构建，打包命令`package -Dmaven.test.skip=true`。

* 打成`jar`包后，运行命令

  ```java -jar package_name.jar ipaddr:port path_of_znode initial_exchange_rate_config_file.yml currency_name```

  例如

  ```java -jar updater-1.0-jar-with-dependencies.jar 127.0.0.1:2181 /zk_test exchange_rate.yml RMB```

  它将会监视`ipaddr:port`对应的ip和端口，以`path_of_znode`为目标节点，从`initial_exchange_rate_config_file.yml`中读取汇率初始值，并以`currency_name`作为货币名称。

