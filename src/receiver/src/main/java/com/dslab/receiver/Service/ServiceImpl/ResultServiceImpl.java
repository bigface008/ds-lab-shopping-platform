package com.dslab.receiver.Service.ServiceImpl;

import com.dslab.receiver.Controller.ResponseParam.OrderIdGenerator;
import com.dslab.receiver.DAO.ResultRepository;
import com.dslab.receiver.Service.ResultService;
import com.dslab.receiver.model.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ResultServiceImpl implements ResultService{
    @Autowired
    private ResultRepository resultRepository;

    @Override
    public ResultEntity getResult(String id) {
        ResultEntity result;
        try {
            result = resultRepository.findById(id).get();
            return result;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public OrderIdGenerator getOrderIdGenerator() {
        return new OrderIdGenerator();
    }
}
