#!/bin/bash

WORKDIR=/home/ubuntu
CALC=$WORKDIR/calculator

SUBMIT=$WORKDIR/spark-2.4.3-bin-hadoop2.7/bin/spark-submit
MASTER=spark://{spark-host-port-list}

$SUBMIT --class com.dslab.Calculator --deploy-mode cluster --master $MASTER $CALC/calculator.jar $CALC/config.json $CALC/hibernate.cfg.xml
