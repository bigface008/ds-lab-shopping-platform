#!/bin/sh
set -e

HOST=ubuntu@{remote-ip}
PORT=30xx$1

scp -P $PORT -r kafka $HOST:~/
ssh -p $PORT $HOST "cd ~/kafka; ./deploy.sh "$1
