package com.airtribe.chronos.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.airtribe.chronos.entity.Job;
import com.airtribe.chronos.entity.JobStatus;

public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByStatusAndScheduleTimeBefore(JobStatus status, Instant time);
    List<Job> findByStatusInAndScheduleTimeBefore(List<JobStatus> statuses, Instant time);
    List<Job> findByStatusInAndScheduleTimeLessThanEqual(List<JobStatus> statuses, Instant time);

    
}
