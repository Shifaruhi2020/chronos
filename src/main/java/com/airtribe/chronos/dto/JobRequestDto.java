package com.airtribe.chronos.dto;

import lombok.Data;
import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

@Data
public class JobRequestDto {
    @NotBlank(message = "Type is required")
    private String type;

    @FutureOrPresent(message = "Schedule time must be in the future or present")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime scheduleTime;

    private String cronExpression;
    
    private Map<String, Object> parameters;
}
