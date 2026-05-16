package com.contract.entity;

import com.contract.enums.ApprovalNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalRecord {
    private String id;
    private String contractId;
    private ApprovalNode node;
    private String approver;
    private Boolean approved;
    private String opinion;
    private LocalDateTime createdAt;
}
