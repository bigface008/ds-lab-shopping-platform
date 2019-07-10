package com.dslab.receiver.Controller.ResponseParam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderIdGenerator {
    static private String server_id = "";

    private long count = 0;

    private long timestamp = System.currentTimeMillis();

    public OrderIdGenerator() {}

    @Value("${server_id}")
    public void setServerId(String server_id) {
        OrderIdGenerator.server_id = server_id;
    }

    public String generateId() {
        long time = System.currentTimeMillis();
        if (time == this.timestamp) {
            this.count++;
            if (this.count >= (1 << 16)) {
                while (System.currentTimeMillis() == this.timestamp) ;
                this.count = 0;
                this.timestamp = System.currentTimeMillis();
            }
        } else {
            this.timestamp = time;
        }
        return String.valueOf((this.timestamp << 20) + (Long.parseLong(OrderIdGenerator.server_id) << 16) + this.count);
    }


}
