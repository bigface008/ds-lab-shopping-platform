package com.dslab.receiver.Controller.RequestParam;

import com.dslab.receiver.Controller.ResponseParam.SendOrder;

public class ReceiveOrder {
    private String user_id;
    private String initiator;
    private OrderItem[] items;
    private String time;

    public ReceiveOrder(String user_id, String initiator, OrderItem[] items, String time) {
        this.user_id = user_id;
        this.initiator = initiator;
        this.items = items;
        this.time = time;
    }

    public SendOrder change2SendOrder() {
        return new SendOrder(this.user_id, this.initiator, this.items);
    }

    @Override
    public String toString() {
        return "{user_id: " + this.user_id + "; initiator: " + this.initiator +
                "; items: " + this.items.toString() + "; time: " + this.time + ";}";
    }
}
