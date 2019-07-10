package com.dslab.receiver.Controller.ResponseParam;

import java.math.BigDecimal;

public class Amount {
    private BigDecimal amount;

    public Amount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
