#!/bin/sh

if [ ! -d "ds-lab-shopping-platform" ]; then
git clone https://github.com/bigface008/ds-lab-shopping-platform.git
else
cd ds-lab-shopping-platform
git pull
cd ..
fi

mkdir -p calculator
if [ ! -f "calculator/config.json"]; then
cp ds-lab-shopping-platform/config/calculator/config.json calculator/
fi
if [ ! -f "calculator/hibernate.cfg.xml"]; then
cp ds-lab-shopping-platform/config/calculator/hibernate.cfg.xml calculator/
fi

cd ds-lab-shopping-platform/src/calculator
mvn package
mv target/calculator-*-jar-with-dependencies.jar ../../../calculator/calculator.jar
