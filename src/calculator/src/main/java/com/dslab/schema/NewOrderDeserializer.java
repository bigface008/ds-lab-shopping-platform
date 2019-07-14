package com.dslab.schema;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class NewOrderDeserializer implements Deserializer<NewOrder> {
    public NewOrderDeserializer() {}

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public NewOrder deserialize(String topic, byte[] bytes) {
        return JSON.parseObject(bytes, NewOrder.class);
    }

    @Override
    public void close() {}
}
