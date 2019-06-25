# 准备工作

## 创建实例

在云平台上基于`ubuntu-x86_64-16.04`镜像创建4个实例，命名为`ds-1`, `ds-2`, `ds-3`, `ds-4`，添加ssh key便于登录实例。

为`ds-1`分配浮动ip `192.168.2.xx`。

修改默认安全组，添加进入规则，放开全部入站流量。

## 登录ds-1

利用创建实例时录入的ssh key登录实例：`ssh ubuntu@platform-ip -p 30xx0`。

修改`/etc/hosts`添加解析：

```
# /etc/hosts

127.0.0.1 localhost
127.0.0.1 ds-1
{internel-ip-of-ds-2} ds-2
{internel-ip-of-ds-3} ds-3
{internel-ip-of-ds-4} ds-4

# ...
```

这样就可以用机器名而非平台随机分配的ip代指各台机器了

## ssh转发

由于平台只给了一个浮动ip，只能直接连接到其中一台机器(分配给了`ds-1`)。
为了能够方便地登陆每台机器，需要在`ds-1`建立端口转发。

考虑到后续项目决定采用nginx做负载均衡，所以方便起见这里的端口转发也用nginx来实现。

在`ds-1`上安装nginx：`sudo apt install nginx`。
修改nginx配置文件`/etc/nginx/nginx.conf`，添加转发规则

```
# /etc/nginx/nginx.conf
# ...

stream {
  server {
    listen 30xx1;
    proxy_pass ds-1:22;
  }
  server {
    listen 30xx2;
    proxy_pass ds-2:22;
  }
  server {
    listen 30xx3;
    proxy_pass ds-3:22;
  }
  server {
    listen 30xx4;
    proxy_pass ds-4:22;
  }
}
```

重启nginx服务 `sudo service nginx restart`

## 登陆其他实例

现在可以用`ssh ubuntu@platform-ip -p 30xxN`来登陆`ds-N`了。
依次登陆`ds-2`, `ds-3`, `ds-4`, 参照`ds-1`修改它们的`/etc/hosts`

## 准备工作端口映射总结

1. 平台已经做好映射：
   - platform-ip:30xx0   ->   floating-ip:22
   - platform-ip:30xx1-30xx9   ->  floating-ip:30xx1-30xx9
2. 绑定floating-ip到`ds-1`
3. 在`ds-1`上利用nginx添加的映射：
   - ds-1:30xx1   ->   ds-1:22
   - ds-1:30xx2   ->   ds-2:22
   - ds-1:30xx3   ->   ds-3:22
   - ds-1:30xx4   ->   ds-4:22
4. 所以目前有
   - platform-ip:30xx0   ->   ds-1:22
   - platform-ip:30xx1   ->   ds-1:22
   - platform-ip:30xx2   ->   ds-2:22
   - platform-ip:30xx3   ->   ds-3:22
   - platform-ip:30xx4   ->   ds-4:22
