package com.airtribe.chronos.service;

import com.airtribe.chronos.dto.ExecutionLogDto;
import com.airtribe.chronos.dto.JobRequestDto;
import com.airtribe.chronos.dto.JobRescheduleRequest;
import com.airtribe.chronos.dto.JobResponseDto;
import com.airtribe.chronos.entity.ExecutionLog;
import com.airtribe.chronos.entity.Job;
import com.airtribe.chronos.entity.JobStatus;
import exception.JobStateConflictException;
import com.airtribe.chronos.repository.JobRepository;
import com.airtribe.chronos.logging.JobExecutionLogger;

import exception.JobNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionLogger logger;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_BASE = 2; // seconds, base for exponential backoff

    public JobService(JobRepository jobRepository, JobExecutionLogger logger) {
        this.jobRepository = jobRepository;
        this.logger = logger;
    }
    
    private void validateCronExpression(String cronExpression) {
        if (cronExpression != null && !CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }
    }
    
    private void handleJobFailure(Job job, Exception e) {
        job.setRetryCount(job.getRetryCount() + 1);
        if (job.getRetryCount() >= MAX_RETRIES) {
            job.setStatus(JobStatus.FAILED);
            logger.logError(job, "Job failed permanently after " + MAX_RETRIES + " retries: " + e.getMessage());
        } else {
            // Exponential backoff for retries
            int delaySeconds = (int) Math.pow(RETRY_DELAY_BASE, job.getRetryCount());
            job.setScheduleTime(Instant.now().plusSeconds(delaySeconds));
            job.setStatus(JobStatus.PENDING);
            logger.logWarning(job, "Job failed, scheduled retry #" + (job.getRetryCount() + 1) + " in " + delaySeconds + " seconds");
        }
        jobRepository.save(job);
    }
    
    private boolean canExecuteJob(Job job) {
        return job.getStatus() != JobStatus.CANCELLED && 
               job.getStatus() != JobStatus.COMPLETED &&
               job.getStatus() != JobStatus.PAUSED;
    }

    private JobResponseDto mapToDto(Job job) {
        return JobResponseDto.builder()
                .id(job.getId())
                .type(job.getType())
                .status(job.getStatus().name())
                .scheduleTime(
                        job.getScheduleTime() != null
                                ? job.getScheduleTime().atZone(ZoneId.systemDefault())
                                : null
                )
                .cronExpression(job.getCronExpression())
                .retryCount(job.getRetryCount())
                .executionLogs(
                        job.getExecutionLogs().stream()
                                .map(log -> ExecutionLogDto.builder()
                                        .timestamp(log.getTimestamp())
                                        .message(log.getMessage())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }

    public JobResponseDto createJob(JobRequestDto jobRequest) {
        validateCronExpression(jobRequest.getCronExpression());
        
        Job job = Job.builder()
            .type(jobRequest.getType())
            .cronExpression(jobRequest.getCronExpression())
            .scheduleTime(jobRequest.getScheduleTime() != null 
                ? jobRequest.getScheduleTime().toInstant()
                : Instant.now().plusSeconds(60))
            .parameters(jobRequest.getParameters())
            .build();
            
        logger.logInfo(job, "Job created");
        return mapToDto(jobRepository.save(job));
    }

    // Add to JobService
    public JobResponseDto createRecurringJob(JobRequestDto request) {
        validateCronExpression(request.getCronExpression());
        Job job = Job.builder()
            .type(request.getType())
            .cronExpression(request.getCronExpression())
            .status(JobStatus.PENDING)
            .parameters(request.getParameters())
            .build();
        return mapToDto(jobRepository.save(job));
    }

    public JobResponseDto getJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        return mapToDto(job);
    }

    public List<JobResponseDto> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JobResponseDto updateJob(String id, JobRequestDto jobRequest) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        if (jobRequest.getScheduleTime() != null) {
            existingJob.setScheduleTime(jobRequest.getScheduleTime().toInstant());
        }
        if (jobRequest.getCronExpression() != null) {
            existingJob.setCronExpression(jobRequest.getCronExpression());
        }
        if (jobRequest.getType() != null) {
            existingJob.setType(jobRequest.getType());
        }

        return mapToDto(jobRepository.save(existingJob));
    }

    public void deleteJob(String id) {
        if (!jobRepository.existsById(id)) {
            throw new JobNotFoundException(id);
        }
        jobRepository.deleteById(id);
    }

    public JobResponseDto executeJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        if (!canExecuteJob(job)) {
            throw new IllegalStateException("Job " + id + " cannot be executed. Current status: " + job.getStatus());
        }

        job.setStatus(JobStatus.RUNNING);
        logger.logInfo(job, "Job execution started");

        try {
            performExecution(job);
            logger.logInfo(job, "Job execution completed successfully");

            if (job.getCronExpression() != null) {
                // Recurring job: calculate next run time
                CronExpression cron = CronExpression.parse(job.getCronExpression());
                ZonedDateTime nextRun = cron.next(ZonedDateTime.now());
                if (nextRun != null) {
                    job.setScheduleTime(nextRun.toInstant());
                }
                job.setStatus(JobStatus.PENDING); // keep it pending for next run
            } else {
                // One-time job: mark as completed
                job.setStatus(JobStatus.COMPLETED);
            }

        } catch (Exception e) {
            handleJobFailure(job, e);
        }
        
        return mapToDto(jobRepository.save(job));
    }

    public void executePendingJobs() {
        List<Job> jobsToRun = jobRepository.findByStatusInAndScheduleTimeBefore(
                List.of(JobStatus.PENDING, JobStatus.FAILED),
                Instant.now()
        );

        for (Job job : jobsToRun) {
            executeJob(job.getId());
        }
    }

    public Job rerunJob(String jobId, Instant scheduleTime) {
        Job originalJob = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        Job newJob = new Job();
        newJob.setType(originalJob.getType());
        newJob.setCronExpression(originalJob.getCronExpression());
        newJob.setStatus(JobStatus.PENDING);
        newJob.setScheduleTime(scheduleTime != null ? scheduleTime : Instant.now());
        newJob.setRetryCount(0);
        newJob.setExecutionLogs(new ArrayList<>());

        return jobRepository.save(newJob);
    }

    // For testing/mocking
    protected void performExecution(Job job) {
        if (Math.random() < 0.3) {
            throw new RuntimeException("Simulated failure");
        }
        // Add actual execution logic here
    }

    @Scheduled(fixedDelay = 3000) // every 30 seconds
    public void schedulePendingJobs() {
        try {
            executePendingJobs();
        } catch (Exception e) {
            // Log errors if execution fails
            System.out.println("Error executing pending jobs: " + e.getMessage());
        }
    }

    // In JobService.java
    public JobResponseDto cancelJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw new IllegalStateException("Job with id " + id + " is already " + job.getStatus());
        }

        job.setStatus(JobStatus.CANCELLED);
        job.getExecutionLogs().add(new ExecutionLog(LocalDateTime.now(), "Job cancelled by user"));

        Job savedJob = jobRepository.save(job);
        return mapToDto(savedJob);
    }

    // In JobService.java
    public JobResponseDto rescheduleJob(String id, JobRescheduleRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));

        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw new IllegalStateException("Job with id " + id + " cannot be rescheduled. Current status: " + job.getStatus());
        }

        if (request.getNewScheduleTime() != null) {
            job.setScheduleTime(request.getNewScheduleTime());
            job.setCronExpression(null); // ensure it's treated as one-time job
        }

        if (request.getNewCronExpression() != null) {
            job.setCronExpression(request.getNewCronExpression());
        }

        job.setStatus(JobStatus.PENDING);
        job.getExecutionLogs().add(new ExecutionLog(LocalDateTime.now(), "Job rescheduled by user"));

        Job savedJob = jobRepository.save(job);
        return mapToDto(savedJob);
    }

    // Runs every 30 seconds to pick up pending one-time jobs and recurring jobs
    @Scheduled(fixedRate = 30000)
    public void runPendingJobs() {
        try {
            Instant now = Instant.now();
            logger.logInfo(null, "Checking for pending jobs...");

            List<Job> pendingJobs = jobRepository.findByStatusInAndScheduleTimeLessThanEqual(
                List.of(JobStatus.PENDING),
                now
            );

            if (pendingJobs == null || pendingJobs.isEmpty()) {
                logger.logInfo(null, "No pending jobs found");
                return;
            }

            logger.logInfo(null, "Found " + pendingJobs.size() + " pending jobs");
            for (Job job : pendingJobs) {
                if (job == null) {
                    logger.logError(null, "Found null job in pending jobs list");
                    continue;
                }
                
                try {
                    if (!canExecuteJob(job)) {
                        logger.logInfo(job, "Skipping job - not executable in current state");
                        continue;
                    }

                    // Mark as running immediately to avoid duplicate execution
                    job.setStatus(JobStatus.RUNNING);
                    jobRepository.save(job);
                    logger.logInfo(job, "Starting job execution");
                    executeJob(job.getId());
                } catch (Exception e) {
                    logger.logError(job, "Error executing job: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.logError(null, "Error processing pending jobs: " + e.getMessage());
        }
    }

    public Job pauseJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (job.getStatus() == JobStatus.COMPLETED) {
            throw new JobStateConflictException("Job with id " + id + " is already COMPLETED");
        }

        job.setStatus(JobStatus.PAUSED);
        job.getExecutionLogs().add(
                new ExecutionLog(LocalDateTime.now(), "Job paused by user")
        );
        return jobRepository.save(job);
   }

    public Job resumeJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (job.getStatus() != JobStatus.PAUSED) {
            throw new JobStateConflictException("Job with id " + id + " is not in PAUSED state");
        }

        job.setStatus(JobStatus.PENDING);
        job.getExecutionLogs().add(
                new ExecutionLog(LocalDateTime.now(), "Job resumed by user")
        );
        return jobRepository.save(job);
    }
}

