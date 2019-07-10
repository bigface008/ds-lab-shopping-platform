package com.dslab.receiver.Service;

import com.dslab.receiver.Controller.ResponseParam.OrderIdGenerator;
import com.dslab.receiver.model.ResultEntity;
import org.springframework.stereotype.Service;

@Service
public interface ResultService {
    ResultEntity getResult(String id);
    OrderIdGenerator getOrderIdGenerator();
}
