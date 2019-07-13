#!/bin/sh

while [ $# != 0 ]; do
    sed "s/CURRENCY/"$1"/g" < config.yml > config_$1.yml
    nohup java -jar updater.jar config_$1.yml >>updater_$1.log 2>&1 &
    shift
done
