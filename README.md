# Distributed System Lab - Distributed Transaction Settlement System
## 1 Design
### 1.1 Overview

> TODO: Describe the goal of the system and the workflow through each module.

### 1.2 Data structures

- 新订单(json, 用于传输)

   |Field|Description|
   |--|--|
   |`user_id` _string_|用户ID|
   |`initiator` _string_|用户使用的货币, 可以是 `RMB`, `USD`, `JPY`, `EUR`|
   |`time` _int_|发送订单的时间戳|
   |`items` _[]_|货物列表|
   |`items[].id` _int_|货物ID|
   |`items[].number` _int_|货物数量|

- 商品(sql, 存储在mysql中)

   |Field|Description|
   |--|--|
   |`id` _int(10) unsigned_|ID|
   |`name` _varchar(200)_|名字|
   |`price` _decimal(65,30)_|价格|
   |`currency` _varchar(100)_|价格使用的货币|
   |`inventory` _int(10) unsigned_|库存|

- 订单结果(sql, 存储在mysql中)

   |Field|Description|
   |--|--|
   |`id` _varchar(100)_|订单ID|
   |`user_id` _varchar(100)_|用户ID|
   |`initiator` _varchar(100)_|用户使用的货币|
   |`success` _tinyint(1)_|是否成功, 为null则表示处理中|
   |`paid` _decimal(65,30_|以用户使用的货币的应付款|

- 成交额(sql, 存储在mysql中)

   |Field|Description|
   |--|--|
   |`currency` _varchar(100)_|货币|
   |`amount` _decimal(65,30)_|全期成交额总量|

- 汇率(存储在zookeeper中的/exchange_rate结点下)

   |Node|Initial value|
   |--|--|
   |/RMB|2.00|
   |/USD|12.00|
   |/JPY|0.15|
   |/EUR|9.00|

### 1.3 Sender

从文件中读取订单信息并按照约定的接口(HTTP/json)将订单发送至 Receiver

### 1.4 Receiver/HTTP server/Kafka producer

1. 从 Sender 接受新订单请求，生成订单号并将新订单放入 Kafka
2. 响应查询 amount 的请求返回指定货币的订单成交额总和

### 1.5 Exchange rate updater

随机生成汇率并将指定货币的汇率写入 Zookeeper 对应的 znode 中

### 1.6 Kafka consumer/Spark driver

> TODO: Describe how it works.

## 2 Implement

1. [Sender](./doc/sender.md)
2. [Receiver](./doc/receiver.md)
3. [Exchange rate updater](./doc/exchange_rate_updater.md)

## 3 Deployment

> TODO: Put here links refering to deployment/operation logs(.md files).

1. [Zookeeper Cluster](./doc/zookeeper.md)
2. [Kafka Cluster](./doc/kafka.md)
3. [DB cluster: ProxySQL + MySQL](./doc/mysql.md)
4. [Receiver](./doc/receiver.md#部署)
5. [Exchange rate updater](./doc/exchange_rate_updater.md#部署)


## 4 Demo

> TODO: Run a demo and put here its result.

## 5 Management

[项目管理](./doc/management.md)

## 6 Members
| Student ID   | Name   |
| ------------ | ------ |
| 5142609052   | 沈小洲 |
| 516030910313 | 宋博仪 |
| 516030910459 | 邵欣阳 |
| 516030910460 | 汪喆昊 |
