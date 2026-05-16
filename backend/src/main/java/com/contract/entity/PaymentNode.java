package com.contract.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentNode {
    private String id;
    private String name;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String description;
    private boolean completed;
}
