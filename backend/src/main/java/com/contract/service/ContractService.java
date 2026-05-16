package com.contract.service;

import com.contract.dto.ContractStatistics;
import com.contract.entity.ApprovalRecord;
import com.contract.entity.Contract;
import com.contract.entity.PaymentNode;
import com.contract.entity.PerformanceTask;
import com.contract.enums.ApprovalNode;
import com.contract.enums.ContractStatus;
import com.contract.enums.TaskStatus;
import com.contract.store.DataStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContractService {

    public Contract createContract(Contract contract) {
        contract.setId(UUID.randomUUID().toString());
        contract.setCode("CT" + System.currentTimeMillis());
        contract.setStatus(ContractStatus.DRAFT);
        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        contract.setCurrentNodeIndex(0);
        DataStore.CONTRACTS.put(contract.getId(), contract);
        return contract;
    }

    public Contract submitApproval(String contractId) {
        Contract contract = DataStore.CONTRACTS.get(contractId);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        List<ApprovalNode> flow = generateApprovalFlow(contract.getAmount());
        contract.setApprovalFlow(flow);
        contract.setCurrentNodeIndex(0);
        contract.setStatus(ContractStatus.PENDING_APPROVAL);
        moveToNextNode(contract);
        contract.setUpdatedAt(LocalDateTime.now());
        return contract;
    }

    private List<ApprovalNode> generateApprovalFlow(BigDecimal amount) {
        List<ApprovalNode> flow = new ArrayList<>();
        flow.add(ApprovalNode.LEGAL);
        flow.add(ApprovalNode.FINANCIAL);
        if (amount.compareTo(new BigDecimal("100000")) >= 0) {
            flow.add(ApprovalNode.MANAGER);
        }
        if (amount.compareTo(new BigDecimal("500000")) >= 0) {
            flow.add(ApprovalNode.DIRECTOR);
        }
        return flow;
    }

    private void moveToNextNode(Contract contract) {
        if (contract.getCurrentNodeIndex() >= contract.getApprovalFlow().size()) {
            contract.setStatus(ContractStatus.APPROVED);
            contract.setCurrentApprover(null);
        } else {
            ApprovalNode nextNode = contract.getApprovalFlow().get(contract.getCurrentNodeIndex());
            if (nextNode == ApprovalNode.LEGAL) {
                contract.setStatus(ContractStatus.LEGAL_REVIEW);
                contract.setCurrentApprover("法务专员");
            } else if (nextNode == ApprovalNode.FINANCIAL) {
                contract.setStatus(ContractStatus.FINANCIAL_REVIEW);
                contract.setCurrentApprover("财务专员");
            } else if (nextNode == ApprovalNode.MANAGER) {
                contract.setCurrentApprover("经理");
            } else if (nextNode == ApprovalNode.DIRECTOR) {
                contract.setCurrentApprover("总监");
            }
        }
    }

    public List<String> validateLegalClauses(Contract contract) {
        List<String> missingClauses = new ArrayList<>();
        List<String> requiredLegalClauses = Arrays.asList(
            "违约责任", "争议解决", "保密条款", "生效条件"
        );
        for (String clause : requiredLegalClauses) {
            if (contract.getRequiredClauses() == null || 
                !contract.getRequiredClauses().contains(clause)) {
                missingClauses.add(clause);
            }
        }
        return missingClauses;
    }

    public List<String> validatePaymentNodes(Contract contract) {
        List<String> issues = new ArrayList<>();
        if (contract.getPaymentNodes() == null || contract.getPaymentNodes().isEmpty()) {
            issues.add("付款节点不能为空");
            return issues;
        }
        BigDecimal totalPaymentAmount = BigDecimal.ZERO;
        for (PaymentNode node : contract.getPaymentNodes()) {
            if (node.getAmount() == null || node.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                issues.add("付款节点金额必须大于0");
            }
            if (node.getDueDate() == null) {
                issues.add("付款节点日期不能为空");
            }
            totalPaymentAmount = totalPaymentAmount.add(node.getAmount() != null ? node.getAmount() : BigDecimal.ZERO);
        }
        if (totalPaymentAmount.compareTo(contract.getAmount()) > 0) {
            issues.add("付款节点总金额不能超过合同总金额");
        }
        return issues;
    }

    public void approveContract(String contractId, String approver, String opinion) {
        Contract contract = DataStore.CONTRACTS.get(contractId);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        ApprovalNode currentNode = contract.getApprovalFlow().get(contract.getCurrentNodeIndex());
        if (currentNode == ApprovalNode.LEGAL) {
            List<String> missing = validateLegalClauses(contract);
            if (!missing.isEmpty()) {
                throw new RuntimeException("必填条款不完整，缺少: " + String.join(", ", missing));
            }
        }
        if (currentNode == ApprovalNode.FINANCIAL) {
            List<String> issues = validatePaymentNodes(contract);
            if (!issues.isEmpty()) {
                throw new RuntimeException("付款节点校验失败: " + String.join(", ", issues));
            }
        }
        ApprovalRecord record = new ApprovalRecord();
        record.setId(UUID.randomUUID().toString());
        record.setContractId(contractId);
        record.setNode(currentNode);
        record.setApprover(approver);
        record.setApproved(true);
        record.setOpinion(opinion);
        record.setCreatedAt(LocalDateTime.now());
        DataStore.APPROVAL_RECORDS.put(record.getId(), record);
        contract.getApprovalRecords().add(record);
        contract.setCurrentNodeIndex(contract.getCurrentNodeIndex() + 1);
        moveToNextNode(contract);
        if (contract.getStatus() == ContractStatus.APPROVED) {
            markContractEffective(contractId);
        }
        contract.setUpdatedAt(LocalDateTime.now());
    }

    public void rejectContract(String contractId, String approver, String opinion) {
        Contract contract = DataStore.CONTRACTS.get(contractId);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        ApprovalNode currentNode = contract.getApprovalFlow().get(contract.getCurrentNodeIndex());
        ApprovalRecord record = new ApprovalRecord();
        record.setId(UUID.randomUUID().toString());
        record.setContractId(contractId);
        record.setNode(currentNode);
        record.setApprover(approver);
        record.setApproved(false);
        record.setOpinion(opinion);
        record.setCreatedAt(LocalDateTime.now());
        DataStore.APPROVAL_RECORDS.put(record.getId(), record);
        contract.getApprovalRecords().add(record);
        contract.setStatus(ContractStatus.REJECTED);
        contract.setCurrentApprover(null);
        contract.setUpdatedAt(LocalDateTime.now());
    }

    public Contract markContractEffective(String contractId) {
        Contract contract = DataStore.CONTRACTS.get(contractId);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        contract.setStatus(ContractStatus.EFFECTIVE);
        contract.setUpdatedAt(LocalDateTime.now());
        generatePerformanceTasks(contract);
        return contract;
    }

    private void generatePerformanceTasks(Contract contract) {
        if (contract.getPaymentNodes() == null) {
            return;
        }
        for (PaymentNode node : contract.getPaymentNodes()) {
            PerformanceTask task = new PerformanceTask();
            task.setId(UUID.randomUUID().toString());
            task.setContractId(contract.getId());
            task.setContractName(contract.getName());
            task.setPaymentNodeId(node.getId());
            task.setTaskName("履约付款: " + node.getName());
            task.setAmount(node.getAmount());
            task.setDueDate(node.getDueDate());
            task.setOwner(contract.getCreator());
            task.setStatus(TaskStatus.PENDING);
            task.setReminderSent(false);
            task.setCreatedAt(LocalDateTime.now());
            DataStore.PERFORMANCE_TASKS.put(task.getId(), task);
        }
    }

    public List<Contract> getAllContracts() {
        return new ArrayList<>(DataStore.CONTRACTS.values());
    }

    public Contract getContractById(String id) {
        return DataStore.CONTRACTS.get(id);
    }

    public Contract updateContract(Contract contract) {
        Contract existing = DataStore.CONTRACTS.get(contract.getId());
        if (existing == null) {
            throw new RuntimeException("合同不存在");
        }
        contract.setUpdatedAt(LocalDateTime.now());
        DataStore.CONTRACTS.put(contract.getId(), contract);
        return contract;
    }

    public ContractStatistics getStatistics() {
        ContractStatistics stats = new ContractStatistics();
        List<Contract> contracts = getAllContracts();
        List<PerformanceTask> tasks = new ArrayList<>(DataStore.PERFORMANCE_TASKS.values());
        stats.setTotalCount((long) contracts.size());
        stats.setTotalAmount(contracts.stream()
            .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        long effectiveCount = contracts.stream()
            .filter(c -> c.getStatus() == ContractStatus.EFFECTIVE || c.getStatus() == ContractStatus.RISK)
            .count();
        stats.setEffectiveCount(effectiveCount);
        long pendingCount = contracts.stream()
            .filter(c -> c.getStatus() == ContractStatus.PENDING_APPROVAL || 
                        c.getStatus() == ContractStatus.LEGAL_REVIEW || 
                        c.getStatus() == ContractStatus.FINANCIAL_REVIEW)
            .count();
        stats.setPendingCount(pendingCount);
        long expiredCount = tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.EXPIRED)
            .count();
        stats.setExpiredCount(expiredCount);
        long completedTasks = tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
            .count();
        stats.setPerformanceRate(tasks.isEmpty() ? 100.0 : (double) completedTasks / tasks.size() * 100);
        return stats;
    }
}
