package com.dslab.receiver.Controller.ResponseParam;

import com.alibaba.fastjson.annotation.JSONField;

public class OrderId {
    @JSONField(name = "id")
    private String id;

    public OrderId(String order_id) {
        this.id = order_id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
