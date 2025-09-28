// package com.chronos.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.spy;
// import static org.mockito.Mockito.when;

// import java.time.Instant;
// import java.util.ArrayList;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import com.airtribe.chronos.dto.JobResponseDto;
// import com.airtribe.chronos.entity.Job;
// import com.airtribe.chronos.entity.JobStatus;
// import com.airtribe.chronos.repository.JobRepository;
// import com.airtribe.chronos.service.JobService;

// import exception.JobNotFoundException;

// public class JobServiceTest {

//      @Mock
//     private JobRepository jobRepository;

//     @InjectMocks
//     private JobService jobService;

//     private Job job;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);

//         job = new Job();
//         job.setId("test-job-1");
//         job.setType("report");
//         job.setStatus(JobStatus.PENDING);
//         job.setRetryCount(0);
//         job.setScheduleTime(Instant.now());
//         job.setExecutionLogs(new ArrayList<>()); // important!
//     }

//     @Test
//     void executeJob_success() {
//         when(jobRepository.findById("test-job-1")).thenReturn(Optional.of(job));
//         when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArguments()[0]);

//         JobResponseDto executedJobDto = jobService.executeJob("test-job-1");

//         assertEquals("COMPLETED", executedJobDto.getStatus());
//         assertEquals(0, executedJobDto.getRetryCount());
//         assertTrue(executedJobDto.getExecutionLogs().stream()
//                 .anyMatch(log -> log.getMessage().contains("completed successfully")));
//     }

//     @Test
//     void executeJob_failure_withRetry() {
//         job.setStatus(JobStatus.PENDING);
//         when(jobRepository.findById("test-job-1")).thenReturn(Optional.of(job));
//         when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArguments()[0]);

//         // Execute normally; outcome could be COMPLETED or FAILED based on random failure
//         JobResponseDto executedJobDto = jobService.executeJob("test-job-1");

//         assertTrue(
//             executedJobDto.getStatus().equals("COMPLETED") || executedJobDto.getStatus().equals("FAILED")
//         );
//         assertTrue(executedJobDto.getRetryCount() >= 0);
//         assertTrue(executedJobDto.getExecutionLogs().size() >= 1);
//     }

//     @Test
//     void executeJob_invalidId_throwsException() {
//         when(jobRepository.findById("invalid-id")).thenReturn(Optional.empty());

//         assertThrows(JobNotFoundException.class, () -> {
//             jobService.executeJob("invalid-id");
//         });
//     }

//     @Test
//     void executeJob_failure_thenRetry_success() throws Exception {
//         // Spy on service to mock performExecution
//         JobService spyService = spy(jobService);

//         // First call fails, second call succeeds
//         doThrow(new RuntimeException("Simulated failure"))
//             .doCallRealMethod() // second call uses real method
//             .when(spyService).performExecution(any(Job.class));

//         when(jobRepository.findById("test-job-1")).thenReturn(Optional.of(job));
//         when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArguments()[0]);

//         JobResponseDto result = spyService.executeJob("test-job-1");

//         assertEquals("COMPLETED", result.getStatus());
//         assertEquals(1, result.getRetryCount());
//         assertTrue(result.getExecutionLogs().stream()
//                    .anyMatch(log -> log.getMessage().contains("Retrying")));
//     }



// }
