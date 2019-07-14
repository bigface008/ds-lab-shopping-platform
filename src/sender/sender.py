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
HOST = sys.argv[1]
PORT = sys.argv[2]
POST_ORDER_URL = "http://" + HOST + ":" + PORT + "/order"
GET_ORDER_URL = POST_ORDER_URL + "/"
GET_AMOUNT_URL = "http://" + HOST + ":" + PORT + "/amount/"
CURRENCIES = ["RMB", "USD", "JPY", "EUR"]

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
headers = {'Content-Type': 'application/json'}
for order in orders:
    # Send orders.
    # print order
    print json.dumps(order)
    r = requests.post(POST_ORDER_URL, data=json.dumps(order), headers=headers)
    if r.status_code != requests.codes.accepted:
        print "Post of order", i, "failed"
        i += 1
        continue
    order_id = r.json()["id"]
    print "Post of order", order_id, "response code:", r.status_code, "content:", r.json()

    # Get result.
    while True:
        time.sleep(1)
        # r = requests.get(GET_ORDER_URL + '%s' % order_id)
        r = requests.get(GET_ORDER_URL + '%s' % order_id)

        if r.status_code != requests.codes.ok:
            print "Get of", order_id, "order failed with wrong status code."
            continue
        if_succeed = r.json()["success"]
        if r.json()["paid"] is None:
            continue
        if if_succeed == True:
            print "Get of order", order_id, "response code:", r.status_code, "content:", r.json()
            break
        elif if_succeed == False:
            print "Get of order", order_id, "failed with report of failure."
            break
        else:
            pass

    i += 1

# Get amount.
for currency in CURRENCIES:
    while True:
        time.sleep(1)
        r = requests.get(GET_AMOUNT_URL + currency)
        if r.status_code != requests.codes.ok:
            print "Get of", currency, "failed with wrong status code."
            continue
        else:
            print "Get of", currency, "response code:", r.status_code, "content:", r.json()
            break
