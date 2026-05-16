package com.contract.dto;

import lombok.Data;

@Data
public class ApprovalRequest {
    private String contractId;
    private String approver;
    private Boolean approved;
    private String opinion;
}
