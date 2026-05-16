package com.contract.entity;

import com.contract.enums.TaskStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PerformanceTask {
    private String id;
    private String contractId;
    private String contractName;
    private String paymentNodeId;
    private String taskName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String owner;
    private TaskStatus status;
    private boolean reminderSent;
    private LocalDateTime createdAt;
}
