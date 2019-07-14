package com.dslab;

import com.alibaba.fastjson.JSON;
import com.dslab.schema.*;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Calculator {
    public static void main(String[] args) {
        String appName = "PayingCalculator";
        String kafkaUrl = "ds-1:9092,ds-2:9092,ds-3:9092,ds-4:9092";
        String topic = "dslab-new-order";
        String zkUrl = "ds-1:2181,ds-2:2181,ds-3:2181";
        String znode = "/exchange_rate";

        SparkConf conf = new SparkConf().setAppName(appName);
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", kafkaUrl);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", NewOrderDeserializer.class);
        kafkaParams.put("group.id", appName);
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Collections.singletonList(topic);

        JavaInputDStream<ConsumerRecord<String, NewOrder>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferBrokers(),
                ConsumerStrategies.Subscribe(topics, kafkaParams));

        // commit offset after get rdd
        stream.foreachRDD(rdd -> {
            ((CanCommitOffsets) stream.inputDStream()).commitAsync(((HasOffsetRanges) rdd.rdd()).offsetRanges());
        });

        // get new order from message record
        JavaDStream<NewOrder> newOrder = stream.map(record -> {
            Session session = new Configuration().configure(new File("hibernate.cfg.xml")).buildSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            NewOrder order = record.value();
            ResultEntity result = new ResultEntity();
            result.setId(order.getId());
            result.setUserId(order.getUserId());
            result.setInitiator(order.getInitiator());
            result.setSuccess(null);
            result.setPaid(null);
            session.save(result);
            tx.commit();
            session.close();
            return order;
        });

        JavaDStream<NewOrder> successOrder = newOrder.filter(order -> {
            Session session = new Configuration().configure(new File("hibernate.cfg.xml")).buildSessionFactory().openSession();

            // exactly once
            ResultEntity result = session.get(ResultEntity.class, order.getId());
            if (result.getSuccess() != null) {
                session.close();
                return result.getSuccess().equals((byte)1);
            }

            Transaction tx = session.beginTransaction();
            ArrayList<CommodityEntity> handled = new ArrayList<>();
            for (OrderItem item: order.getItems()) {
                CommodityEntity c = session.get(CommodityEntity.class, Integer.parseInt(item.getId()));
                // if no such commodity or no enough inventory
                if (c == null || c.getInventory() < item.getNumber()) {
                    result.setSuccess((byte)0);
                    result.setPaid(new BigDecimal(0));
                    session.save(result);
                    tx.commit();
                    session.close();
                    System.out.println("[OrderFail] " + JSON.toJSONString(order));
                    return false;
                } else {
                    c.setInventory(c.getInventory() - item.getNumber());
                    handled.add(c);
                }
            }
            for (CommodityEntity c: handled) {
                session.save(c);
            }
            result.setSuccess((byte)1);
            session.save(result);
            tx.commit();
            session.close();
            System.out.println("[OrderSuccess] " + JSON.toJSONString(order));
            return true;
        });

        successOrder.foreachRDD(rdd -> rdd.foreachPartition(partitionOrders -> {
            if (!partitionOrders.hasNext()) {
                return;
            }
            Session session = new Configuration().configure(new File("hibernate.cfg.xml")).buildSessionFactory().openSession();
            ExchangeRateFetcher fetcher = new ExchangeRateFetcher(zkUrl, znode);

            while (partitionOrders.hasNext()) {
                NewOrder order = partitionOrders.next();
                ResultEntity result = session.get(ResultEntity.class, order.getId());

                // exactly once
                if (result.getPaid() != null) {
                    continue;
                }
                BigDecimal paid = new BigDecimal(0).setScale(5, RoundingMode.HALF_EVEN);
                for (OrderItem item: order.getItems()) {
                    CommodityEntity c = session.get(CommodityEntity.class, Integer.parseInt(item.getId()));
                    BigDecimal cRate = fetcher.getRate(c.getCurrency());
                    BigDecimal uRate = fetcher.getRate(order.getInitiator());
                    BigDecimal cPaid = c.getPrice().multiply(new BigDecimal(item.getNumber()));
                    cPaid = cPaid.multiply(cRate).divide(uRate, RoundingMode.HALF_EVEN);
                    paid = paid.add(cPaid.setScale(5, RoundingMode.HALF_EVEN));
                }
                result.setPaid(paid);
                Transaction tx = session.beginTransaction();
                session.save(result);
                tx.commit();
                System.out.println("[OrderPaid] " + order.getId() + ": " + paid.toString());
            }

            fetcher.close();
            session.close();
        }));

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
