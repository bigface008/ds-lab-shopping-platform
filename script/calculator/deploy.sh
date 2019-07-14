#!/bin/sh
set -e

HOST=ubuntu@{remote-ip}
for i in $(seq 1 4); do
PORT=30xx$i
scp -P $PORT -r calculator $HOST:~/
done
scp -P 30xx1 start-calculator.sh $HOST:~/calculator/
ssh -p 30xx1 $HOST "cd ~/calculator; ./start-calculator.sh "
