#!/bin/sh
set -e

HOST=ubuntu@{remote-ip}
PORT=30xx$1

scp -P 30xx$1 -r kafka $HOST:~/
ssh -p 30xx$1 ubuntu@{remote-ip} "cd ~/kafka; ./deploy.sh "$1
