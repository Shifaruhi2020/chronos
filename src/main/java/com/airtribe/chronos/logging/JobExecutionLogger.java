package com.airtribe.chronos.logging;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.airtribe.chronos.entity.ExecutionLog;
import com.airtribe.chronos.entity.Job;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobExecutionLogger {
    
    public enum LogLevel {
        INFO,
        WARNING,
        ERROR
    }

    public void logJobExecution(Job job, String message, LogLevel level) {
        ExecutionLog log = ExecutionLog.builder()
            .timestamp(LocalDateTime.now())
            .message(message)
            .build();
        
        if (job != null && job.getExecutionLogs() != null) {
            job.getExecutionLogs().add(log);
        }
        
        // Also log to console for system-level messages when job is null
        String prefix = job != null ? "[Job " + job.getId() + "] " : "[System] ";
        switch (level) {
            case ERROR:
                System.err.println(prefix + message);
                break;
            default:
                System.out.println(prefix + message);
        }
    }

    public void logInfo(Job job, String message) {
        logJobExecution(job, message, LogLevel.INFO);
    }

    public void logWarning(Job job, String message) {
        logJobExecution(job, message, LogLevel.WARNING);
    }

    public void logError(Job job, String message) {
        logJobExecution(job, message, LogLevel.ERROR);
    }
}