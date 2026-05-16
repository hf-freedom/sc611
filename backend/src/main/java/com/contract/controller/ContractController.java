package com.contract.controller;

import com.contract.dto.ApprovalRequest;
import com.contract.dto.ContractStatistics;
import com.contract.entity.Contract;
import com.contract.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping
    public ResponseEntity<Contract> createContract(@RequestBody Contract contract) {
        Contract created = contractService.createContract(contract);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> updateContract(@PathVariable String id, @RequestBody Contract contract) {
        contract.setId(id);
        Contract updated = contractService.updateContract(contract);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitApproval(@PathVariable String id) {
        try {
            Contract contract = contractService.submitApproval(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", contract);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveContract(@RequestBody ApprovalRequest request) {
        try {
            contractService.approveContract(request.getContractId(), request.getApprover(), request.getOpinion());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "审批成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectContract(@RequestBody ApprovalRequest request) {
        try {
            contractService.rejectContract(request.getContractId(), request.getApprover(), request.getOpinion());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "驳回成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable String id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ContractStatistics> getStatistics() {
        return ResponseEntity.ok(contractService.getStatistics());
    }

    @GetMapping("/{id}/validate-legal")
    public ResponseEntity<List<String>> validateLegalClauses(@PathVariable String id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contractService.validateLegalClauses(contract));
    }

    @GetMapping("/{id}/validate-payment")
    public ResponseEntity<List<String>> validatePaymentNodes(@PathVariable String id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contractService.validatePaymentNodes(contract));
    }
}
