# Receiver

主要用于接收Sender发来的请求，并将新的order信息发送给`Kafka`集群。

## API

- `/order`

   Receiver接收Sender发来的新order的信息，并且发送该`Kafka`集群。完成后返回新的order的`id`。

- `/order/{id}`

   Receiver接收Sender发来的消息`id`，据此查询相应的order并返回给Sender。

- `/order/{currency}`

   Receiver接收Sender发来的消息`currency`，据此查询相应的amount并返回给Sender。

## 使用方法

1. 将相应的`receiver.jar`复制到相应服务器上，在同个目录的`config`文件夹下面放置`application.properties`。

       cp /path/to/receiver/src/main/resources/application.properties ./config/application.properties

2. 运行如下命令：

   ```shell
   java -jar receiver.jar
   ```

   receiver即启动完毕。

## 配置选项

一般情况下，需要修改的配置包括以下几个。

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

   表示当前服务器的序号。本项目中部署Receiver的服务器序号为。
