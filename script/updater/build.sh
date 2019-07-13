#!/bin/sh

if [ ! -d "ds-lab-shopping-platform" ];then
git clone https://github.com/bigface008/ds-lab-shopping-platform.git
fi

mkdir -p updater
if [ ! -f "updater/config.yml" ];then
cat updater/config.yml
cp ds-lab-shopping-platform/config/updater/config.yml updater/
fi
cp start-updater.sh updater/

cd ds-lab-shopping-platform/src/updater
mvn package -Dmaven.test.skip=true
mv target/updater-*-jar-with-dependencies.jar ../../../updater/updater.jar
