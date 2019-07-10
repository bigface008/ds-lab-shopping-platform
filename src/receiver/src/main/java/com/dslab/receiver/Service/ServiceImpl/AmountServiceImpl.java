package com.dslab.receiver.Service.ServiceImpl;

import com.dslab.receiver.Controller.ResponseParam.Amount;
import com.dslab.receiver.DAO.AmountRepository;
import com.dslab.receiver.Service.AmountService;
import com.dslab.receiver.model.AmountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AmountServiceImpl implements AmountService {
    @Autowired
    private AmountRepository amountRepository;

    @Override
    public Amount getAmount(String currency) {
        try {
            AmountEntity amount = amountRepository.findAmountByCurrency(currency).get();
            return new Amount(amount.getAmount());
        } catch (NoSuchElementException e) {
            return new Amount(BigDecimal.valueOf(-1.0));
        }
    }

    @Override
    public List<AmountEntity> getAllAmountEntities() {
        return amountRepository.findAll();
    }
}
