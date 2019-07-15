package com.dslab;

import com.alibaba.fastjson.JSON;
import com.dslab.schema.*;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkFiles;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Calculator {
    public static void main(String[] args) throws IOException {
        CalculatorConfig calcConf = JSON.parseObject(new FileInputStream(args[0]), CalculatorConfig.class);
        JavaStreamingContext jssc = new JavaStreamingContext(new SparkConf().setAppName(calcConf.getAppName()), Durations.seconds(1));

        Collection<String> topics = Collections.singletonList(calcConf.getTopic());
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", calcConf.getKafkaUrl());
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", NewOrderDeserializer.class);
        kafkaParams.put("group.id", calcConf.getAppName());
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("enable.auto.commit", false);

        JavaInputDStream<ConsumerRecord<String, NewOrder>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferBrokers(),
                ConsumerStrategies.Subscribe(topics, kafkaParams));

        // commit offset after get rdd
        stream.foreachRDD(rdd -> ((CanCommitOffsets) stream.inputDStream()).commitAsync(((HasOffsetRanges) rdd.rdd()).offsetRanges()));

        // get new order from message record
        // save order to result with success and paid set to null
        stream
        .map(record -> {
            NewOrder order = record.value();
            ResultEntity result = new ResultEntity();
            result.setId(order.getId());
            result.setUserId(order.getUserId());
            result.setInitiator(order.getInitiator());
            result.setSuccess(null);
            result.setPaid(null);

            HiberSession hs = new HiberSession(args[1]);
            hs.beginTransaction();
            if (hs.session().get(ResultEntity.class, result.getId()) == null) {
                hs.session().save(result);
            }
            hs.commit();
            hs.close();

            return order;
        })
        .filter(order -> {
            HiberSession hs = new HiberSession(args[1]);

            // do exactly once
            ResultEntity result = hs.session().get(ResultEntity.class, order.getId());
            if (result.getSuccess() != null) {
                hs.close();
                return result.getSuccess().equals((byte)1);
            }

            // collect commodity whose inventory has been changed
            ArrayList<CommodityEntity> handled = new ArrayList<>();
            // sort items by ID to avoid dead lock in mysql
            ArrayList<OrderItem> items = order.getItems();
            items.sort(Comparator.comparing(OrderItem::getId));

            hs.beginTransaction();
            for (OrderItem item: order.getItems()) {
                CommodityEntity c = hs.session().get(CommodityEntity.class, Integer.parseInt(item.getId()));
                // if no such commodity or no enough inventory
                if (c == null || c.getInventory() < item.getNumber()) {
                    result.setSuccess((byte)0);
                    result.setPaid(new BigDecimal(0));
                    hs.session().save(result);
                    hs.commit();
                    hs.close();
                    System.out.println("[OrderFail] " + JSON.toJSONString(order));
                    return false;  // won't appear at next rdd
                } else {
                    c.setInventory(c.getInventory() - item.getNumber());
                    handled.add(c);
                }
            }
            // save the changes
            for (CommodityEntity c: handled) {
                hs.session().save(c);
            }
            result.setSuccess((byte)1);
            hs.session().save(result);
            hs.commit();
            hs.close();
            System.out.println("[OrderSuccess] " + JSON.toJSONString(order));
            return true;  // calculate paid at next rdd
        })
        .foreachRDD(rdd -> rdd.foreachPartition(partitionOrders -> {
            // to avoid useless connection to database
            if (!partitionOrders.hasNext()) {
                return;
            }
            HiberSession hs = new HiberSession(args[1]);
            ExchangeRateFetcher fetcher = new ExchangeRateFetcher(calcConf.getZkUrl(), calcConf.getZnode());

            while (partitionOrders.hasNext()) {
                NewOrder order = partitionOrders.next();
                ResultEntity result = hs.session().get(ResultEntity.class, order.getId());

                // do exactly once
                if (result.getPaid() != null) {
                    continue;
                }

                BigDecimal paid = new BigDecimal(0).setScale(5, RoundingMode.HALF_EVEN);
                for (OrderItem item: order.getItems()) {
                    CommodityEntity c = hs.session().get(CommodityEntity.class, Integer.parseInt(item.getId()));
                    // rate of commodity currency to union currency
                    BigDecimal cRate = fetcher.getRate(c.getCurrency());
                    // rate of customer currency to union currency
                    BigDecimal uRate = fetcher.getRate(order.getInitiator());
                    // paid = paid + price * number * cRate / uRate
                    BigDecimal cPaid = c.getPrice().multiply(new BigDecimal(item.getNumber()));
                    cPaid = cPaid.multiply(cRate).divide(uRate, RoundingMode.HALF_EVEN);
                    paid = paid.add(cPaid.setScale(5, RoundingMode.HALF_EVEN));
                }
                result.setPaid(paid);
                // save result and amount to database
                hs.beginTransaction();
                hs.session().save(result);
                AmountEntity amount = hs.session().get(AmountEntity.class, order.getInitiator());
                if (amount == null) {
                    amount = new AmountEntity();
                    amount.setCurrency(order.getInitiator());
                    amount.setAmount(paid);
                } else {
                    // amount = amount + paid
                    amount.setAmount(amount.getAmount().add(paid));
                }
                hs.session().save(amount);
                hs.commit();
                System.out.println("[OrderPaid] " + order.getId() + ": " + paid.toString());
            }

            fetcher.close();
            hs.close();
        }));

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
