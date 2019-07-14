package com.dslab.schema;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;

@Data
public class NewOrder {
    @JSONField(name = "id")
    private String id;
    @JSONField(name = "user_id")
    private String userId;
    @JSONField(name = "initiator")
    private String initiator;
    @JSONField(name = "time")
    private long time;
    @JSONField(name = "items")
    private ArrayList<OrderItem> items;
}
