package com.contract.service;

import com.contract.entity.Contract;
import com.contract.entity.PerformanceTask;
import com.contract.enums.ContractStatus;
import com.contract.enums.TaskStatus;
import com.contract.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceService {

    public List<PerformanceTask> getAllTasks() {
        return new ArrayList<>(DataStore.PERFORMANCE_TASKS.values());
    }

    public List<PerformanceTask> getTasksByContractId(String contractId) {
        List<PerformanceTask> result = new ArrayList<>();
        for (PerformanceTask task : DataStore.PERFORMANCE_TASKS.values()) {
            if (contractId.equals(task.getContractId())) {
                result.add(task);
            }
        }
        return result;
    }

    public void completeTask(String taskId) {
        PerformanceTask task = DataStore.PERFORMANCE_TASKS.get(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setStatus(TaskStatus.COMPLETED);
    }

    public void checkAndMarkExpired() {
        LocalDate today = LocalDate.now();
        for (PerformanceTask task : DataStore.PERFORMANCE_TASKS.values()) {
            if (task.getStatus() == TaskStatus.PENDING && task.getDueDate().isBefore(today)) {
                task.setStatus(TaskStatus.EXPIRED);
                Contract contract = DataStore.CONTRACTS.get(task.getContractId());
                if (contract != null) {
                    contract.setRisk(true);
                    contract.setStatus(ContractStatus.RISK);
                }
            }
        }
    }

    public List<PerformanceTask> getTasksForReminder() {
        List<PerformanceTask> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(3);
        for (PerformanceTask task : DataStore.PERFORMANCE_TASKS.values()) {
            if (task.getStatus() == TaskStatus.PENDING && 
                !task.isReminderSent() && 
                !task.getDueDate().isAfter(reminderDate)) {
                result.add(task);
            }
        }
        return result;
    }

    public void markReminderSent(String taskId) {
        PerformanceTask task = DataStore.PERFORMANCE_TASKS.get(taskId);
        if (task != null) {
            task.setReminderSent(true);
        }
    }

    public List<String> getReminders() {
        return new ArrayList<>(DataStore.REMINDERS);
    }
}
