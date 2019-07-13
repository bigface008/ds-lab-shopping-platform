# exchange rate updater

用于更新存储在 zookeeper 中的汇率表。

按照lab要求，我们使用虚拟的汇率，即程序自动生成汇率。

我们每次将汇率更新为初始值上下 10% 的一个随机值。

## 运行

```
java -jar updater-1.0.jar config.yml
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
