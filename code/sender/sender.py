# -*- coding: UTF-8 -*-
# The format of command should be `python sender.py {host} {port} {file}`, ex:
#     python sender.py 127.0.0.1 5000 orders.json

import requests
import json
import sys
import time

# Check the number of arguments.
if len(sys.argv) != 4:
    print "Wrong arguments."
    exit()

# Read arguments.
host = sys.argv[1]
port = sys.argv[2]
post_url = "http://" + host + ":" + port + "/order"
get_url = post_url + "/"
# url = host + ":" + port + "/order"
fn = sys.argv[3]
fo = open(fn, "r")
content = json.load(fo)
fo.close()

# Build the list of orders.
if type(content).__name__ == "dict":
    orders = [content]
    print "Number of orders: 1"
elif type(content).__name__ == "list":
    orders = content
    print "Number of orders:", len(content)

# Send orders and get results.
i = 0
for order in orders:
    # Send orders.
    r = requests.post(post_url, data=order)
    order_id = r.json()["id"] # This line may fail if the server responds with wrong status code.
    print "Post of order", order_id, "response code:", r.status_code, "content:", r.json()

    # Get result.
    while True:
        time.sleep(5)
        r = requests.get(get_url + '%d' % order_id)
        if_succeed = r.json()["success"]
        if if_succeed == True:
            break
        elif if_succeed == False:
            print "Get of order" + order_id + "failed."
            exit()
        else:
            pass
    print "Get of order", order_id, "response code:", r.status_code, "content:", r.json()
    i += 1
