package com.contract.task;

import com.contract.entity.PerformanceTask;
import com.contract.service.PerformanceService;
import com.contract.store.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PerformanceService performanceService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendPerformanceReminders() {
        logger.info("执行履约提醒定时任务: {}", LocalDateTime.now().format(formatter));
        List<PerformanceTask> tasks = performanceService.getTasksForReminder();
        for (PerformanceTask task : tasks) {
            String reminder = String.format("[提醒] 合同: %s, 履约任务: %s, 到期日: %s, 负责人: %s",
                task.getContractName(),
                task.getTaskName(),
                task.getDueDate(),
                task.getOwner());
            logger.info(reminder);
            DataStore.REMINDERS.add(LocalDateTime.now().format(formatter) + " " + reminder);
            performanceService.markReminderSent(task.getId());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredTasks() {
        logger.info("执行逾期检查定时任务: {}", LocalDateTime.now().format(formatter));
        performanceService.checkAndMarkExpired();
    }
}
