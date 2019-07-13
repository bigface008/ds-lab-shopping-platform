#!/bin/sh

KAFKA="kafka_2.12-2.2.0"
set -e

wget -N http://mirror.bit.edu.cn/apache/kafka/2.2.0/$KAFKA.tgz
tar -xzf $KAFKA.tgz
rm $KAFKA.tgz

sed "s/BROKERID/"$1"/g" < kafka.properties > $KAFKA/config/cluster.properties
mkdir $KAFKA/log
sudo mv $KAFKA /opt/kafka

sudo mkdir -p /usr/lib/systemd/system
sudo mv kafka.service /usr/lib/systemd/system/

sudo systemctl enable kafka.service
sudo systemctl start kafka.service
