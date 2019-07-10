package com.dslab.receiver.Controller;

import com.dslab.receiver.Controller.ResponseParam.Amount;
import com.dslab.receiver.Service.AmountService;
import com.dslab.receiver.model.AmountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AmountController {
    @Autowired
    private AmountService amountService;

    @GetMapping(value = "/amount/{currency}")
    public Amount getAmount(@PathVariable("currency")String currency) {
        System.out.println("GET /amount/: amount: " + amountService.getAmount(currency));
        return amountService.getAmount(currency);
    }

    @GetMapping(value = "/amountall")
    public List<AmountEntity> getAllAmountEntities() {
        return amountService.getAllAmountEntities();
    }
}
