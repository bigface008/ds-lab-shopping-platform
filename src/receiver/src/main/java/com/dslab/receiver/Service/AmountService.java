package com.dslab.receiver.Service;

import com.dslab.receiver.Controller.ResponseParam.Amount;
import com.dslab.receiver.model.AmountEntity;

import java.util.List;

public interface AmountService {
    Amount getAmount(String currency);
    List<AmountEntity> getAllAmountEntities();
}
