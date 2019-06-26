# Sender 接口
沈小洲 2019/6/26 19:48:00

## Input
- Example of order (from file as json)
```json
    {
      "user_id": 123456,
      "initiator": "RMB",
      "time": 1558868400000,
      "items": [
        {"id": "1", "number": 2},
        {"id": "3", "number": 1}
      ]
    }
```
- Host (from sys.args)
- Port (from sys.args)

## Internel call
### POST
#### Request
- POST `http://{host}:{port}/order`
- Content-type: `application/json`
- order from input as request body
#### Response
- 202 `{"id": "str"}` 请求成功
- 4xx `{"error": "str", "details": ["line1", "line2", ...]}` 请求出错
- 5xx 服务出错
### GET
#### Request
- GET `http://{host}:{port}/order/{id}`
#### Response
- 200 `{"id": "str", "user_id": int, "initiator": "str", "success": null/true/false, "paid": float}` null表示还在处理中

## output
- Result (to screen)