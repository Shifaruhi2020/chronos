package com.airtribe.chronos.dto;

import java.time.Instant;

import lombok.Data;

// DTO for reschedule request
@Data
public class JobRescheduleRequest {
    private Instant newScheduleTime; // optional
    private String newCronExpression; // optional
}

