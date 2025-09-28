package com.airtribe.chronos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemHealth {
    private String status;
    private long activeJobs;
    private long completedJobs;
    private long failedJobs;
    private double averageExecutionTime;
    private String lastCheckTime;
}