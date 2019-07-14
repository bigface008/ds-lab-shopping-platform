package com.dslab;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

@Data
public class CalculatorConfig implements Serializable {
    @JSONField(name = "kafka-url")
    private String kafkaUrl;
    @JSONField(name = "topic")
    private String topic = "dslab-new-order";
    @JSONField(name = "consumer-group")
    private String appName = "PayingCalculator";
    @JSONField(name = "zookeeper-url")
    private String zkUrl;
    @JSONField(name = "exchange-rate-znode")
    private String znode = "/exchange-rate";
}
