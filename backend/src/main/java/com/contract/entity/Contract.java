package com.contract.entity;

import com.contract.enums.ApprovalNode;
import com.contract.enums.ContractStatus;
import com.contract.enums.ContractType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Contract {
    private String id;
    private String name;
    private String code;
    private ContractType type;
    private BigDecimal amount;
    private String partyA;
    private String partyB;
    private LocalDate startDate;
    private LocalDate endDate;
    private String content;
    private List<String> requiredClauses;
    private List<PaymentNode> paymentNodes;
    private String creator;
    private String currentApprover;
    private ContractStatus status;
    private List<ApprovalNode> approvalFlow;
    private Integer currentNodeIndex;
    private List<ApprovalRecord> approvalRecords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isRisk;

    public Contract() {
        this.approvalRecords = new ArrayList<>();
        this.paymentNodes = new ArrayList<>();
        this.requiredClauses = new ArrayList<>();
        this.isRisk = false;
    }
}
