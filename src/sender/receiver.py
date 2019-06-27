from flask import Flask, request, make_response
import json

app = Flask(__name__)
i = 0
k = 0


@app.route('/order', methods=["post"])
def order():
    print(request.get_json())
    global i
    i += 1
    return make_response(json.dumps({"id": i}), 202)


@app.route('/order/<int:order_id>', methods=["get"])
def get_order(order_id):
    return json.dumps({"id": order_id, "user_id": 1, "initiator": "str", "success": True, "paid": 1})
    # print "get_order"
    # global k
    # k = 1 - k
    # if k == 0:
    #     print "k = 0"
    #     return json.dumps({"id": order_id, "user_id": 1, "initiator": "str", "success": True, "paid": 1})
    # else:
    #     print "k = 1"
    #     return json.dumps({"id": order_id, "user_id": 1, "initiator": "str", "success": None, "paid": 1})


app.run()