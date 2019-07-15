package com.dslab.receiver.Controller;

import com.dslab.receiver.Controller.RequestParam.ReceiveOrder;
import com.dslab.receiver.Controller.ResponseParam.OrderId;
import com.dslab.receiver.Controller.ResponseParam.OrderIdGenerator;
import com.dslab.receiver.Controller.ResponseParam.SendOrder;
import com.dslab.receiver.Controller.ResponseParam.Sender;
import com.dslab.receiver.Service.ResultService;
import com.dslab.receiver.model.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

@RestController
public class ResultController {
    @Autowired
    private ResultService resultService;

    @Autowired
    private OrderIdGenerator gen;

    @PostMapping(value = "/order")
    public OrderId senderNewOrder(@RequestBody ReceiveOrder receiveOrder, HttpServletResponse response) {
        // Pack up ReceiveOrder object to SendOrder.
        SendOrder sendOrder = receiveOrder.change2SendOrder();
        sendOrder.setOrderId(gen.generateId());

        System.out.println("POST /order: " + sendOrder.toString());

        // Send order to Kafka.
        (new Sender()).send(sendOrder);

        // Send order id to sender.
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        return sendOrder.generateOrderIdObj();
    }

    @GetMapping(value = "/order/{id}")
    public ResultEntity getResult(@PathVariable("id") String id) {
        System.out.println("GET /order/: id: " + id);
        ResultEntity resultEntity = resultService.getResult(id);
        if (resultEntity == null) {
            resultEntity = new ResultEntity(id, "", "", null, new BigDecimal(0.0));
        }
        return resultEntity;
    }
}
