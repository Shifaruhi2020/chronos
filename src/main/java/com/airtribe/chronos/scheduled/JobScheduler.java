package com.airtribe.chronos.scheduled;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import com.airtribe.chronos.entity.Job;
import com.airtribe.chronos.entity.JobStatus;
import com.airtribe.chronos.repository.JobRepository;
import com.airtribe.chronos.service.JobService;
import com.airtribe.chronos.logging.JobExecutionLogger;

@Component
public class JobScheduler {

    private final JobService jobService;
    private final JobRepository jobRepository;
    private final JobExecutionLogger logger;

    public JobScheduler(JobService jobService, JobRepository jobRepository, JobExecutionLogger logger) {
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.logger = logger;
    }

    @Scheduled(fixedRate = 1000)
    public void processJobs() {
        try {
            processOneTimeJobs();
            processRecurringJobs();
        } catch (Exception e) {
            logger.logError(null, "Error processing jobs: " + e.getMessage());
        }
    }

    private void processOneTimeJobs() {
        Instant now = Instant.now();
        List<Job> dueJobs = jobRepository.findAll().stream()
            .filter(job -> job.getStatus() == JobStatus.PENDING)
            .filter(job -> job.getCronExpression() == null)
            .filter(job -> job.getScheduleTime() != null && !job.getScheduleTime().isAfter(now))
            .toList();

        for (Job job : dueJobs) {
            try {
                logger.logInfo(job, "Processing one-time job");
                jobService.executeJob(job.getId());
            } catch (Exception e) {
                logger.logError(job, "Failed to process one-time job: " + e.getMessage());
            }
        }
    }

    private void processRecurringJobs() {
        Instant now = Instant.now();
        List<Job> recurringJobs = jobRepository.findAll().stream()
            .filter(job -> job.getStatus() == JobStatus.PENDING)
            .filter(job -> job.getCronExpression() != null)
            .filter(job -> {
                try {
                    ZonedDateTime nextRun = CronExpression.parse(job.getCronExpression())
                        .next(ZonedDateTime.now().minusSeconds(1));
                    return nextRun != null && !nextRun.toInstant().isAfter(now);
                } catch (Exception e) {
                    logger.logError(job, "Invalid cron expression: " + e.getMessage());
                    return false;
                }
            })
            .toList();

        for (Job job : recurringJobs) {
            try {
                logger.logInfo(job, "Processing recurring job");
                jobService.executeJob(job.getId());
            } catch (Exception e) {
                logger.logError(job, "Failed to process recurring job: " + e.getMessage());
            }
        }
    }
}