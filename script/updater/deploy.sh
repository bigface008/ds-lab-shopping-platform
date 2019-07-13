#!/bin/sh
set -e

HOST=ubuntu@{remote-ip}
PORT=30xx$1
shift

scp -P $PORT -r updater $HOST:~/
ssh -p $PORT $HOST "cd ~/updater; ./start-updater.sh "$*
