# exchange rate updater

用于更新存储在 zookeeper 中的汇率表。

按照lab要求，我们使用虚拟的汇率，即程序自动生成汇率。

我们每次将汇率更新为初始值上下 10% 的一个随机值。

## 构建

```
mvn package
mv target/updater-x.x-jar-with-dependencies.jar ./updater.jar
```

会得到 `updater.jar`

## 运行

```
java -jar updater.jar config.yml
```

## 配置文件

[`config.yml`](../config/updater/config.yml)

- initial: 初始汇率表, 左侧为货币, 右侧为初始汇率
- currency: 要更新汇率的货币, 可以是初始汇率表中任意一个货币
- period: 更新汇率的周期, 以秒为单位
- url: 连接 zookeeper 的host:port对
- znode: 汇率表在zookeeper上存储的结点

## 具体实现

### DistLock 分布式锁

类 `DistLock` 实现了 `Watcher` 和 `StatCallback` 接口。用一个 `ZooKeeper` 实例和用作锁的结点 `znode` 来构造它。

- `acquire` 方法用来获取锁

   先 `tryLock()` 尝试获得锁，若失败了则进入循环调用 `wait()` 直到成功获取锁

- 当 zookeeper 中有事件发生时, `process` 方法会被调用，这里我们判断该事件是否与我们的 znode 有关，若有关则调用 `ZooKeeper` 实例 `zk` 的 `exists` 方法判断该 znode 是否存在。由于该方法是异步的，稍后会回调我们的 `processResult` 方法。在 `processResult` 中若得知结点不存在时，则尝试获得锁 `tryLock()`。

- `tryLock` 会试图创建锁的 znode，若成功则唤醒 `acquire` 中的 `wait()`，使之成功返回。若失败则什么都不做，等待下一次 `process` 和 `processResult` 的调用。

### Updater

`main` 会从第一个参数指定的 yaml 格式的配置文件中读取配置，并用来构建 `Updater` 实例并调用其 `run` 方法。 `Updater` 会构建一个 `Zookeeper` 实例，和 `DistLock` 实例。

`Updater` 本身实现了 `Watcher` 接口，在 `process` 中处理断连消息，并将其他消息发给 `DistLock` 实例。

`run` 是程序的主流程，若跟配置要求的货币对应的 znode 还未被创建，则会创建它。随后获取分布式锁，成功后进入循环：将随机生成的汇率写入 znode，并暂停配置指定的时间。


## 部署

### 部署脚本

- [`build.sh`](../script/updater/build.sh)

   1. 拉取仓库 master 分支，构建 updater 得到 `updater.jar`
   2. 将 `updater.jar`, [`start-updater.sh`](./start-updater.sh), [`config.yml`](../config/updater/config.yml) 放到新建的 `updater` 目录下

- [`deploy.sh`](../script/updater/deploy.sh)

   1. 将 `updater` 目录用 `scp` 命令复制到指定的远端服务器上
   2. 用 `ssh` 命令在远端服务器上执行 `start-updater.sh` 脚本

- [`start-updater.sh`](../script/updater/start-updater.sh)

   每个参数是一个要处理的货币，该脚本会为每一个货币启动一个 updater 来更新汇率。

   循环处理每个参数：
   1. 用参数替换配置文件 `config.yml` 中的 `CURRENCY` 得到 `config_$1.yml`
   2. 以新配置文件启动 `updater.jar`

### 部署命令

在 `script/updater` 目录下

```
./build.sh
```

修改得到的 `updater/config.yml`，填写 zookeeper 的 `url` 和要使用的 `znode`。
注意该 znode 需要提前创建好。设置好更新周期 `period`,以秒为单位。检查初始汇率表。`currency` 保持值为 `CURRENCY`，方便之后的脚本运行。

```
./deploy.sh 1 RMB USD
./deploy.sh 2 JPY EUR
./deploy.sh 3 USD EUR
./deploy.sh 4 RMB JPY
```

这样就分别在 `ds-1`, `ds-4` 部署了更新 RMB 汇率的 updater；在 `ds-1`, `ds-3` 部署了更新 USD 汇率的 updater；在 `ds-2`, `ds-4` 部署了更新 JPY 汇率的 updater；在 `ds-2`, `ds-3` 部署了更新 EUR 汇率的 updater。

### 验证部署

根据 updater 的实现，更新同货币汇率的多个实例，通过 zookeeper 实现的分布式锁来保证最多只有其中一个实例在正常工作，其他的实例监控活跃实例的状态，在活跃实例下线后才会开始工作。

我们决定每种货币部署两个 update 实例到不同的机器上以实现故障转移。

通过每个实例的日志我们可以看出它们的状态。

查看 `ds-1` 上的 `updater_RMB.log`：

```
ubuntu@ds-1:~$ tail -f updater/updater_RMB.log
Waiting for other instance releasing lock.
Start updating exchange rate.
2.01600
2.11600
...
```

查看 `ds-4` 上的 `updater_RMB.log`：

```
ubuntu@ds-4:~$ tail -f updater/updater_RMB.log
Waiting for other instance releasing lock.
```

可见 `ds-1` 上的 updater 在正常地更新 RMB 汇率，而 `ds-4` 上的 updater 作为其备份，在等待 `ds-1` 上的 updater 离线。

查看 zookeeper 上的结点

```
$ zkCli.sh
[zk: localhost:2181(CONNECTED) 0] ls /exchange_rate
[EUR, JPY, RMB, USD]
[zk: localhost:2181(CONNECTED) 1] get /exchange_rate/RMB
1.81600
[zk: localhost:2181(CONNECTED) 2] get /exchange_rate/RMB
1.81600
# 一分钟后
[zk: localhost:2181(CONNECTED) 3] get /exchange_rate/RMB
2.05600
```

可见 znode 中的值确实被 updater 更新了

### 验证故障转移


通过 `ps aux | grep updater` 找到更新 RMB 汇率的 updater 的 PID 并 kill

```
ubuntu@ds-1:~$ ps ax | grep updater
 9524 ?        Sl     0:02 java -jar updater.jar config_RMB.yml
 9526 ?        Sl     0:02 java -jar updater.jar config_USD.yml
10017 pts/0    S+     0:00 grep --color=auto updater
ubuntu@ds-1:~$ kill 9524
ubuntu@ds-1:~$ ps ax | grep updater
 9526 ?        Sl     0:02 java -jar updater.jar config_USD.yml
10021 pts/0    S+     0:00 grep --color=auto updater
```

登录 `ds-4` 查看日志

```
ubuntu@ds-4:~$ tail -f updater/updater_RMB.log
Waiting for other instance releasing lock.
Start updating exchange rate.
2.05600
...
```

可见该 updater 检测到 `ds-1` 的 updater 结束了，于是开始工作
