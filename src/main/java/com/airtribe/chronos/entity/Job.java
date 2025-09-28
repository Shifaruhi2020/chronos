package com.airtribe.chronos.entity;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// since we're using MongoDB @Document = @Entity
// @entity is for JPA
// @Document is for MongoDB
@Document(collection = "jobs")
@CompoundIndexes({
    @CompoundIndex(name = "status_scheduleTime_idx", def = "{'status': 1, 'scheduleTime': 1}")
})
public class Job {

    @Id
    private String id;

    private String userId; // later for auth

    private String type; // e.g. "email", "report", "generic"

    @Builder.Default
    private JobStatus status = JobStatus.PENDING; // pending, running, completed, failed, cancelled

    private Instant scheduleTime; // for one-time jobs

    private String cronExpression; // for recurring jobs (optional)

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private List<ExecutionLog> executionLogs = new ArrayList<>();
    
    private Map<String, Object> parameters; // job-specific configuration

}
