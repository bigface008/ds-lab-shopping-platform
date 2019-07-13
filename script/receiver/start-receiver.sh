#!/bin/sh

sed "s/SERVERID/"$1"/g" < config/application.properties.template > config/application.properties
nohup java -jar receiver.jar >>receiver.log 2>&1 &
