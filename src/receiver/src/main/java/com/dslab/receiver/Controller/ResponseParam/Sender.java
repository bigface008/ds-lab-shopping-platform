package com.dslab.receiver.Controller.ResponseParam;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Sender {
    static private String bootstrapServers;
    static private String topic;

    public Sender() {
    }

    @Value("${spring.kafka.producer.bootstrap-servers}")
    public void setBootstrapServers(String servers) {
        Sender.bootstrapServers = servers;
    }

    @Value("${topic}")
    public void setTopicName(String topic) {
        Sender.topic = topic;
    }

    public void send(SendOrder sendOrder) {
        System.out.println(Sender.bootstrapServers);
        Properties props = new Properties();
        props.put("bootstrap.servers", Sender.bootstrapServers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        System.out.println(JSON.toJSONString(sendOrder));
        producer.send(new ProducerRecord<String, String>(Sender.topic, Integer.toString(0), JSON.toJSONString(sendOrder)));
        producer.close();
    }
}
