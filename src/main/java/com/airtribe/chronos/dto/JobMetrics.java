package com.airtribe.chronos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobMetrics {
    private long totalJobs;
    private long pendingJobs;
    private long runningJobs;
    private long completedJobs;
    private long failedJobs;
    private long pausedJobs;
    private double successRate;
    private double averageExecutionTime;
}