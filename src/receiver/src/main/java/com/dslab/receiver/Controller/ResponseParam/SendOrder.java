package com.dslab.receiver.Controller.ResponseParam;

import com.alibaba.fastjson.annotation.JSONField;
import com.dslab.receiver.Controller.RequestParam.OrderItem;

public class SendOrder {

    @JSONField(name = "id")
    private String orderId;

    @JSONField(name = "user_id")
    private String userId;

    @JSONField(name = "initiator")
    private String initiator;

    @JSONField(name = "items")
    private OrderItem[] items;

    @JSONField(name = "time")
    private long timestamp;

    @JSONField(name = "success")
    private boolean success;

    public SendOrder(String userId, String initiator, OrderItem[] items) {
        this.userId = userId;
        this.initiator = initiator;
        this.items = items;
        this.timestamp = System.currentTimeMillis();
        this.success = false;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public OrderItem[] getItems() {
        return items;
    }

    public void setItems(OrderItem[] items) {
        this.items = items;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        String result = "{id: " + this.orderId + "; user_id: " + this.userId +
                "; initiator: " + this.initiator + "; items: ";
        int i = 0;
        for (OrderItem item : this.items) {
            result += item.toString();
            if (i != this.items.length - 1) {
                result += ", ";
            }
            i++;
        }
        return  result + "; timestamp: " + this.timestamp + ";}";
    }

    public OrderId generateOrderIdObj() {
        return new OrderId(orderId);
    }

    public void send() {
        // TODO: Send order to Kafka.
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");
//        props.put("acks", "all");
//        props.put("retries", 0);
//        props.put("batch.size", 16384);
//        props.put("linger.ms", 1);
//        props.put("buffer.memory", 33554432);
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//
//        Producer<String, String> producer = new KafkaProducer<>(props);
////        for (int i = 0; i < 100; i++)
////            producer.send(new ProducerRecord<String, String>("my-topic", Integer.toString(i), Integer.toString(i)));
//        producer.send(new ProducerRecord<String, String>("my-topic", Integer.toString(0), JSON.toJSONString(this)));
//
//        producer.close();
    }
}
