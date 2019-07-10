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
