# MySQL 集群

## 任务
- 使用[MySQL Group Replication](https://dev.mysql.com/doc/refman/8.0/en/group-replication.html)在三台服务器上搭建高可用的数据库集群，每个服务器上有一个数据库实例运行。

## 步骤
### MySQL Group Replication
1. 在服务器上下载安装MySQL-8.0。
2. 在文件夹`/etc/mysql/mysql.conf.d/`中编写文件`mgr.cnf`。
```conf
[mysqld]
bind-address={ds-3}
report_host={ds-3}
disabled_storage_engines="MyISAM,BLACKHOLE,FEDERATED,ARCHIVE,MEMORY"

server_id=4
gtid_mode=ON
enforce_gtid_consistency=ON
binlog_checksum=NONE

log_bin=binlog
log_slave_updates=ON
binlog_format=ROW
master_info_repository=TABLE
relay_log_info_repository=TABLE

transaction_write_set_extraction=XXHASH64
group_replication_group_name="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
group_replication_start_on_boot=off
group_replication_local_address="{ds-4-ip}:24901"
group_replication_group_seeds="{ds-4-ip}:24901,{ds-3-ip}:24901,{ds-2-ip}:24901"
group_replication_bootstrap_group=off
```
需要注意的是，每个文件中的`bind-address`、`report_hosts`、`group_replication_local_address`都要分别设定，另外，MySQL的root账户默认不能访问远程的MySQL，所以必须创建一个新的、可以访问远程MySQL的账户。

3. 在各台服务器上的MySQL中执行命令
```sql
INSTALL PLUGIN group_replication SONAME 'group_replication.so';
```
这样就安装了Group Replication的插件。
4. 在Primary的MySQL上执行命令
```sql
SET GLOBAL group_replication_bootstrap_group=ON;
START GROUP_REPLICATION;
SET GLOBAL group_replication_bootstrap_group=OFF;
```
5. 在Secondary的MySQL上执行命令
```sql
START GROUP_REPLICATION;
```
6. 过一段时间，在所有服务器的MySQL中执行
```sql
SELECT * FROM performance_schema.replication_group_members;
```
应当可以看到我们的三台服务器都成功上线。
