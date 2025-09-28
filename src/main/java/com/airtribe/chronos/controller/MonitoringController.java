package com.airtribe.chronos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airtribe.chronos.dto.JobMetrics;
import com.airtribe.chronos.dto.SystemHealth;
import com.airtribe.chronos.service.MonitoringService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    
    private final MonitoringService monitoringService;
    
    @GetMapping("/health")
    public SystemHealth getSystemHealth() {
        return monitoringService.getCurrentHealth();
    }
    
    @GetMapping("/metrics")
    public JobMetrics getJobMetrics() {
        return monitoringService.getJobMetrics();
    }
}