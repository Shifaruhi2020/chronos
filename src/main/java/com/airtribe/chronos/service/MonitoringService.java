package com.airtribe.chronos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.airtribe.chronos.dto.JobMetrics;
import com.airtribe.chronos.dto.SystemHealth;
import com.airtribe.chronos.entity.Job;
import com.airtribe.chronos.entity.JobStatus;
import com.airtribe.chronos.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitoringService {
    
    private final JobRepository jobRepository;
    
    public SystemHealth getCurrentHealth() {
        List<Job> allJobs = jobRepository.findAll();
        long activeJobs = allJobs.stream()
            .filter(job -> job.getStatus() == JobStatus.RUNNING)
            .count();
            
        long completedJobs = allJobs.stream()
            .filter(job -> job.getStatus() == JobStatus.COMPLETED)
            .count();
            
        long failedJobs = allJobs.stream()
            .filter(job -> job.getStatus() == JobStatus.FAILED)
            .count();
            
        String status = failedJobs > (completedJobs * 0.1) ? "DEGRADED" : "HEALTHY";
        
        return SystemHealth.builder()
            .status(status)
            .activeJobs(activeJobs)
            .completedJobs(completedJobs)
            .failedJobs(failedJobs)
            .lastCheckTime(LocalDateTime.now().toString())
            .build();
    }
    
    public JobMetrics getJobMetrics() {
        List<Job> allJobs = jobRepository.findAll();
        
        long totalJobs = allJobs.size();
        long pendingJobs = countJobsByStatus(allJobs, JobStatus.PENDING);
        long runningJobs = countJobsByStatus(allJobs, JobStatus.RUNNING);
        long completedJobs = countJobsByStatus(allJobs, JobStatus.COMPLETED);
        long failedJobs = countJobsByStatus(allJobs, JobStatus.FAILED);
        long pausedJobs = countJobsByStatus(allJobs, JobStatus.PAUSED);
        
        double successRate = totalJobs > 0 
            ? (double) completedJobs / totalJobs * 100 
            : 0.0;
            
        return JobMetrics.builder()
            .totalJobs(totalJobs)
            .pendingJobs(pendingJobs)
            .runningJobs(runningJobs)
            .completedJobs(completedJobs)
            .failedJobs(failedJobs)
            .pausedJobs(pausedJobs)
            .successRate(successRate)
            .build();
    }
    
    private long countJobsByStatus(List<Job> jobs, JobStatus status) {
        return jobs.stream()
            .filter(job -> job.getStatus() == status)
            .count();
    }
}