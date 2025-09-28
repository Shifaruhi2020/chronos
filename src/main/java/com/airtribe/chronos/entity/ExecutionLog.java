package com.airtribe.chronos.entity;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLog {
    private LocalDateTime timestamp;
    private String message;

}
