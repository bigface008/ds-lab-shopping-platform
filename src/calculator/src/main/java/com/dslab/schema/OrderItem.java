package com.dslab.schema;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class OrderItem {
    @JSONField(name = "id")
    private String id;
    @JSONField(name = "number")
    private int number;
}
