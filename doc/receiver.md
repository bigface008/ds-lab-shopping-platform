# Receiver

主要用于接收Sender发来的请求，并将新的order信息发送给`Kafka`集群。

## API

- `/order`

   Receiver接收Sender发来的新order的信息，并且发送到`Kafka`集群。完成后返回新的order的`id`。

- `/order/{id}`

   Receiver接收Sender发来的消息`id`，据此查询相应的order并返回给Sender。

- `/order/{currency}`

   Receiver接收Sender发来的消息`currency`，据此查询相应的amount并返回给Sender。

## 构建

用IDEA侧面板中的maven打包或用命令行工具打包：

```
mvn package -Dmaven.test.skip=true
```

最终得到 `target/receiver-x.y.z.jar`

## 配置

receive会读取 `{path-of-receiver.jar}/config/application.properties` 作为配置文件

可以参照 [`receiver/src/main/resources/application.properties.example`](../src/receiver/src/main/resources/application.properties.example) 来编写该配置文件

1. server.port

   表示Receive运行的端口，一般设置为5000。

2. spring.datasource.url

   指示`Spring Boot`连接到`MySQL`上相应的数据库，注意格式为`jdbc:mysql://{host}:{port}/{database}`。

3. spring.datasource.username

   数据库的用户名。

4. spring.datasource.password

   数据库的密码。

5. spring.kafka.producer.bootstrap-servers

   表示`Spring Boot`连接到的`Kafka`集群的地址和端口，如`localhost:9090`。

6. server_id

   表示当前服务器的序号

7. topic

   指定将新订单发送到kafka中的topic

## 部署

采用编写脚本的方式来一键部署

- [build.sh](../script/receiver/build.sh)
   1. 从github拉取源码
   2. `mvn package`打包
   3. 将打包好的`receiver.jar`、源码中的配置文件模板、启动脚本`start-receiver.sh`放在一个目录中

- [start-receiver.sh](../script/receiver/start-receiver.sh)
   1. 将传入的第一个参数作为 `server_id` 填写进配置文件
   2. 启动 `receiver.jar` 并将日志追加写入 `receiver.log`

- [deploy.sh](../script/receiver/deploy.sh)
   1. 根据传入的第一个参数决定要部署在哪个机器
   2. 用 `scp` 将 `build.sh` 生成的文件夹发送到目标机器上
   3. 用 `ssh` 在目标机器上执行 `start-receiver.sh` 脚本

### 部署步骤

1. 构建项目

```
./build.sh
```

2. 参照 [`application.properties`](../config/receiver/application.properties) 修改配置文件模板

```
vim receiver/config/application.properties.example
```

3. 部署到远端

```
./deploy.sh 1
./deploy.sh 3
./deploy.sh 4
```

我们决定部署在 `ds-1`, `ds-3`, `ds-4` 三台机器上

4. 添加 [nginx 配置](../config/nginx/site-available/default) 实现负载均衡

   使得发往端口 30xx5 的HTTP请求会被负载均衡到 `ds-1`, `ds-3`, `ds-4` 的 {receiver-port} 上
