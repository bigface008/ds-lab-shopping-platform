# Spark 集群

## 部署

我们接下来在 `ds-1`, `ds-2`, `ds-3`, `ds-4` 四个虚拟机上部署 Spark 实例。

1. 登录`ds-1`
   ```bash
   $ ssh ubuntu@{platform-ip} -p 30481
   ```

2. 安装spark
  spark无法直接通过apt安装，若使用wget则得不到正确的文件，
  所以我采用了在本地下载对应文件并通过scp将其传至远端。
   ```bash
   $ scp -P 30481 D:/spark-2.4.3-bin-hadoop2.7.tgz ubuntu@202.120.40.8:~
   ```
   然后在远端解压
   ```bash
   $ tar -zxvf spark-2.4.3-bin-hadoop2.7.tgz
   ```
   由于spark需要java，需要安装default-jdk
   ```bash
   $ sudo apt install default-jdk
   ```
   然后设置环境变量
   ```bash
   $ SPARK_HOME=/home/ubuntu/spark-2.4.3-bin-hadoop2.7
   $ PATH="$HOME/bin:$HOME/.local/bin:$PATH:/home/ubuntu/spark-2.4.3-bin-hadoop2.7/bin"
   $ export SPARK_HOME=/home/ubuntu/spark-2.4.3-bin-hadoop2.7
   $ . ~/.profile
   ```
   验证
   ```bash
   $ spark-shell
   ```
   应当出现：
   ```bash
      Welcome to
         ____              __
        / __/__  ___ _____/ /__
       _\ \/ _ \/ _ `/ __/  '_/
      /___/ .__/\_,_/_/ /_/\_\   version 2.4.3
         /_/

   Using Scala version 2.11.12 (OpenJDK 64-Bit Server VM, Java 1.8.0_212)
   Type in expressions to have them evaluated.
   Type :help for more information.

   scala> 
   ```
   依次登陆 `ds-2`, `ds-3`, `ds-4` 执行这一步。
   
3. 编辑配置文件[`~/spark-2.4.3-bin-hadoop2.7/conf/slaves`](../config/spark/slaves)，指定slave的地址，其他配置采用默认值：
   ```
   ds-1
   ds-2
   ds-3
   ds-4
   ```
   
   编辑配置文件[`~/spark-2.4.3-bin-hadoop2.7/conf/spark-env.sh`](../config/spark/spark-env.sh)
   ```
   export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=ds-1:2181,ds-2:2181,ds-3:2181 -Dspark.deploy.zookeeper.dir=/spark"
   export SPARK_WORKER_INSTANCES=1
   export SPARK_WORKER_CORES=3
   export SPARK_MASTER_IP=ds-1
   ```
   此配置文件（spark-env.sh）需要复制到四个虚拟机的对应路径。
4. 启动master（在ds-1和ds-2上）
   ```bash
   $ ./sbin/start-masters.sh
   ```
   启动slaves（在ds-1上）
   ```bash
   $ ./sbin/start-slaves.sh
   ```
