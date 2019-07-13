#!/bin/sh

if [ ! -d "ds-lab-shopping-platform" ];then
git clone https://github.com/bigface008/ds-lab-shopping-platform.git
fi
mkdir -p receiver/config

cd ds-lab-shopping-platform/src/receiver
mvn package -Dmaven.test.skip=true
mv target/receiver-*.jar ../../../receiver/receiver.jar

if [ ! -f "../../../receiver/config/application.properties.example" ];then
mv src/main/resources/application.properties.example ../../../receiver/config/
fi
cd ../../..
cp start-receiver.sh receiver/
