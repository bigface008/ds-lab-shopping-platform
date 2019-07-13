#!/bin/sh
set -e

HOST=ubuntu@{remote-ip}
PORT=30xx$1

scp -P $PORT -r receiver $HOST:~/
ssh -p $PORT $HOST "cd ~/receiver; ./start-receiver.sh "$1
