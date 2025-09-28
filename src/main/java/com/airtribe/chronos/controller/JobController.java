package com.airtribe.chronos.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.airtribe.chronos.dto.JobRequestDto;
import com.airtribe.chronos.dto.JobRescheduleRequest;
import com.airtribe.chronos.dto.JobResponseDto;
import com.airtribe.chronos.entity.Job;
import com.airtribe.chronos.service.JobService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Job Management", description = "APIs for managing scheduled jobs")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Job Management", description = "APIs for managing scheduled jobs")

public class JobController {

   private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new job", description = "Create a new scheduled job with the given parameters")
    public ResponseEntity<JobResponseDto> createJob(@RequestBody @Valid JobRequestDto jobRequest) {
        return ResponseEntity.ok(jobService.createJob(jobRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get job by ID", description = "Retrieve details of a specific job")
    public ResponseEntity<JobResponseDto> getJob(@PathVariable("id")  String id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all jobs", description = "Retrieve a list of all scheduled jobs")
    public ResponseEntity<List<JobResponseDto>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update job", description = "Update an existing job's configuration")
    public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable String id,
            @RequestBody @Valid JobRequestDto jobRequest
    ) {
        return ResponseEntity.ok(jobService.updateJob(id, jobRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete job", description = "Delete a specific job by ID")
    public ResponseEntity<String> deleteJob(@PathVariable("id") String id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job with id " + id + " deleted successfully.");
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Execute job", description = "Manually trigger the execution of a job")
    public ResponseEntity<JobResponseDto> executeJob(@PathVariable("id") String id) {
        return ResponseEntity.ok(jobService.executeJob(id));
    }


    @PostMapping("/{id}/rerun")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Rerun job", description = "Create and execute a new instance of an existing job")
    public ResponseEntity<Job> rerunJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.rerunJob(id, null));
    }


    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancel job", description = "Cancel a running or scheduled job")
    public ResponseEntity<JobResponseDto> cancelJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.cancelJob(id));
    }

    // In JobController.java
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Reschedule job", description = "Update the schedule of an existing job")
    public ResponseEntity<JobResponseDto> rescheduleJob(
            @PathVariable String id,
            @RequestBody JobRescheduleRequest request
    ) {
        return ResponseEntity.ok(jobService.rescheduleJob(id, request));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Job> pauseJob(@PathVariable("id") String id) {
        return ResponseEntity.ok(jobService.pauseJob(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Job> resumeJob(@PathVariable("id") String id) {
        return ResponseEntity.ok(jobService.resumeJob(id));
    }




}
