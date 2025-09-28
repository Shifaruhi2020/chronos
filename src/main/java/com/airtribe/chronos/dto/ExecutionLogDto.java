package com.airtribe.chronos.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionLogDto {
   private LocalDateTime timestamp;
    private String message;
}
