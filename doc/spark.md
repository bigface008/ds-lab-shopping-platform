# Spark 集群

## 部署

获取程序包并解压

```
wget http://mirrors.tuna.tsinghua.edu.cn/apache/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.7.tgz
tar -xf spark-2.4.3-bin-hadoop2.7.tgz
```

添加配置文件 `spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh`

```
cp spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh.template spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh
```

并设置以下值

```
# 设置每个 worker 只能占用 3 个核心
SPARK_WORKER_CORES=3
# 设置 zookeeper 以实现高可用部署
SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url={zkhost-port-list} -Dspark.deploy.zookeeper.dir=/spark"
```

添加配置文件 `spark-2.4.3-bin-hadoop2.7/conf/log4j.properties`

```
cp spark-2.4.3-bin-hadoop2.7/conf/log4j.properties.template spark-2.4.3-bin-hadoop2.7/conf/log4j.properties
```

并修改该值为 WARN 以减少应用的输出

```
log4j.rootCategory=WARN, console
```

在 `ds-1`, `ds-2`, `ds-3`, `ds-4` 上都执行以上操作

在 `ds-1`, `ds-2` 上运行

```
./spark-2.4.3-bin-hadoop2.7/bin/start-master.sh
```

由于设置了 zookeeper 恢复策略，两个master将组成备份集

然后在 `ds-1`, `ds-2`, `ds-3`, `ds-4` 上运行

```
./spark-2.4.3-bin-hadoop2.7/bin/start-slave.sh spark://ds-1:{port},ds-2:{port}
```

共运行 4 个 worker 并连接到两个 master 上。

## 验证部署

配置 nginx 将两个 master 的 web ui 端口暴露出来

```
server {
    listen 30xx8;
    location / {
        proxy_pass http://ds-1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-Ip $remote_addr;
        proxy_set_header X-Forwarded-For $remote_addr;
    }
}
server {
    listen 30xx9;
    location / {
        proxy_pass http://ds-2:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-Ip $remote_addr;
        proxy_set_header X-Forwarded-For $remote_addr;
    }
}
```

通过浏览器打开 `http://{remote-ip}:30xx8` 可以查看 master 1 的状态，可以看到

> Status: ALIVE
>
> Workers (4)

而打开 `http://{remote-ip}:30xx9` 可以查看 master 2 的状态，可以看到

> Status: STANDBY
>
> Workers (0)

这意味着所有 4 个 worker 都登记在 master 1 之上，master 2 作为备份处于不活跃的状态。

## 验证故障转移

通过 `ps aux | grep spark` 找到 master 1 的 PID 并 `kill` 掉

现在无法通过浏览器打开 `http://{remote-ip}:30xx8`

但打开 `http://{remote-ip}:30xx9` 后可以看到

> Status: ALIVE
>
> Workers (4)

即所有 worker 都转移到 master 2 上了
