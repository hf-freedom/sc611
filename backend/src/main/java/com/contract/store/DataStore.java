package com.contract.store;

import com.contract.entity.ApprovalRecord;
import com.contract.entity.Contract;
import com.contract.entity.PerformanceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static final Map<String, Contract> CONTRACTS = new ConcurrentHashMap<>();
    public static final Map<String, ApprovalRecord> APPROVAL_RECORDS = new ConcurrentHashMap<>();
    public static final Map<String, PerformanceTask> PERFORMANCE_TASKS = new ConcurrentHashMap<>();
    public static final List<String> REMINDERS = new ArrayList<>();
}
