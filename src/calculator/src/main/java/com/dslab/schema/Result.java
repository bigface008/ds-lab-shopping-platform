package com.dslab.schema;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Result {
    private String id;
    private String userId;
    private String initiator;
    private Byte success;
    private BigDecimal paid;
}
