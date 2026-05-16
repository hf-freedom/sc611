package com.contract.controller;

import com.contract.entity.PerformanceTask;
import com.contract.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/tasks")
    public ResponseEntity<List<PerformanceTask>> getAllTasks() {
        return ResponseEntity.ok(performanceService.getAllTasks());
    }

    @GetMapping("/tasks/contract/{contractId}")
    public ResponseEntity<List<PerformanceTask>> getTasksByContractId(@PathVariable String contractId) {
        return ResponseEntity.ok(performanceService.getTasksByContractId(contractId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable String taskId) {
        performanceService.completeTask(taskId);
        return ResponseEntity.ok("任务完成");
    }

    @GetMapping("/reminders")
    public ResponseEntity<List<String>> getReminders() {
        return ResponseEntity.ok(performanceService.getReminders());
    }
}
