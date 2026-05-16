package com.contract.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractStatistics {
    private Long totalCount;
    private BigDecimal totalAmount;
    private Double performanceRate;
    private Long expiredCount;
    private Long pendingCount;
    private Long effectiveCount;
}
