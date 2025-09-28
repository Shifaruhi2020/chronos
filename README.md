# chronos
Airtribe Capstone Project

## ⚡ Chronos - Project Features

Chronos is a **Job Scheduling and Monitoring System** built with Spring Boot. It allows users to schedule, manage, and monitor automated jobs with full authentication and role-based access control.

### 🔑 Key Features

1. **Authentication & Authorization**
   - User registration and login.
   - JWT-based authentication for secure API access.
   - Role-based access control using `@PreAuthorize`.

2. **Job Management**
   - Create, update, and delete scheduled jobs.
   - Support for **cron expressions** and **fixed schedule times**.
   - Manual execution of jobs.
   - Pause, resume, cancel, and rerun jobs.
   - Reschedule jobs dynamically.

3. **Job Monitoring & Metrics**
   - Real-time system health check.
   - Job metrics including:
     - Total jobs scheduled
     - Jobs completed
     - Jobs in progress
     - Failed or canceled jobs

4. **RESTful APIs**
   - Well-documented endpoints using **Swagger/OpenAPI**.
   - Sample request and response bodies for each endpoint.
   - Standardized error responses with HTTP status codes.

5. **Validation & Error Handling**
   - Input validation with `@Valid`.
   - Meaningful error messages for invalid input, unauthorized access, or job not found.

6. **Extensibility**
   - Modular design with services and DTOs.
   - Easy to extend for new job types, notification methods, or metrics.

---

## 🚀 Getting Started

### Prerequisites
- Java 17
- Maven
- MongoDB

### Installation & Run
```bash
git clone https://github.com/Shifaruhi2020/chronos.git
cd chronos
mvn clean install
mvn spring-boot:run
```

---


## 🛠 Technologies Used
- **Spring Boot** – REST API development
- **MongoDB** – NoSQL database
- **JWT** – Authentication & Authorization
- **Swagger / OpenAPI** – API documentation
- **Lombok** – Boilerplate reduction
- **Maven** – Dependency management
- **Java 17** – Programming language

---

## 📂 Project Structure

The project follows a modular structure to promote maintainability and scalability:

```plaintext
chronos/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   └── chronos/
│   │   │   │       ├── ChronosApplication.java        # Main application entry point
│   │   │   │       ├── config/                        # Configuration classes
│   │   │   │       ├── controller/                    # REST API controllers
│   │   │   │       ├── dto/                           # Data Transfer Objects
│   │   │   │       ├── exception/                     # Custom exceptions
│   │   │   │       ├── model/                         # Entity models
│   │   │   │       ├── repository/                    # MongoDB repositories
│   │   │   │       ├── security/                      # Security configurations
│   │   │   │       └── service/                       # Business logic services
│   │   └── resources/
│   │       ├── application.properties                # Application properties
│   │       └── static/                               # Static assets (if any)
│   └── test/                                         # Unit and integration tests
├── .gitignore                                       # Git ignore file
├── pom.xml                                          # Maven project configuration
└── README.md                                        # Project documentation
```

---

### 🌐 Monitoring APIs

| Method | Endpoint               | Description                        |
|--------|------------------------|------------------------------------|
| GET    | `/api/v1/monitoring/health`  | Retrieve current system health      |
| GET    | `/api/v1/monitoring/metrics` | Get job execution metrics           |

#### Sample Response - System Health
```json
{
  "uptime": "12 hours",
  "memoryUsage": "512MB",
  "cpuLoad": "35%",
  "status": "HEALTHY"
}
```

#### Sample Response - Job Metrics
```json
{
  "totalJobs": 120,
  "scheduledJobs": 80,
  "completedJobs": 30,
  "failedJobs": 5,
  "pausedJobs": 5
}
```

---

### 🔐 Authentication APIs

| Method | Endpoint              | Description                 |
|--------|----------------------|-----------------------------|
| POST   | `/api/v1/auth/register` | Register a new user          |
| POST   | `/api/v1/auth/login`    | Login and generate JWT token |

#### Sample Request - Register
```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "Test@123",
  "firstName": "Test",
  "lastName": "User"
}
```

#### Sample Response - Register
```json
{
  "message": "User registered successfully",
  "userId": "652f8a2c3f1e2b0012345678",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Sample Request - Login
```json
{
  "username": "testuser",
  "password": "Test@123"
}
```

#### Sample Response - Login
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

✅ Chronos combines **job scheduling, execution, and monitoring** with **secure authentication**, making it ideal for automating backend tasks and tracking system performance.


## 📌 Job Management API

All endpoints require a **JWT token** in the `Authorization` header:

```
Authorization: Bearer <your_token>
```

---

### 1️⃣ Create Job
**POST** `/api/v1/jobs`  
Create a new scheduled job.

#### Sample Request
```json
{
  "type": "EMAIL_NOTIFICATION",
  "cronExpression": "0 */15 * * * ?",
  "scheduleTime": "2025-09-28T20:00:00+05:30",
  "parameters": {
    "recipient": "user@example.com",
    "subject": "Test Job",
    "body": "This is a test notification"
  }
}
```

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "type": "EMAIL_NOTIFICATION",
  "status": "SCHEDULED",
  "cronExpression": "0 */15 * * * ?",
  "scheduleTime": "2025-09-28T20:00:00+05:30",
  "parameters": {
    "recipient": "user@example.com",
    "subject": "Test Job",
    "body": "This is a test notification"
  }
}
```

---

### 2️⃣ Get Job by ID
**GET** `/api/v1/jobs/{id}`  

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "type": "EMAIL_NOTIFICATION",
  "status": "SCHEDULED",
  "cronExpression": "0 */15 * * * ?",
  "scheduleTime": "2025-09-28T20:00:00+05:30",
  "parameters": {
    "recipient": "user@example.com",
    "subject": "Test Job",
    "body": "This is a test notification"
  }
}
```

---

### 3️⃣ Get All Jobs
**GET** `/api/v1/jobs`  

#### Sample Response (200 OK)
```json
[
  {
    "id": "652f8b3d3f1e2b0012345679",
    "type": "EMAIL_NOTIFICATION",
    "status": "SCHEDULED",
    "cronExpression": "0 */15 * * * ?",
    "scheduleTime": "2025-09-28T20:00:00+05:30"
  },
  {
    "id": "652f8c4e3f1e2b0012345680",
    "type": "BACKUP",
    "status": "COMPLETED",
    "scheduleTime": "2025-09-28T18:00:00+05:30"
  }
]
```

---

### 4️⃣ Update Job
**PUT** `/api/v1/jobs/{id}`  

#### Sample Request
```json
{
  "type": "EMAIL_NOTIFICATION",
  "cronExpression": "0 */10 * * * ?",
  "scheduleTime": "2025-09-28T21:00:00+05:30",
  "parameters": {
    "recipient": "admin@example.com",
    "subject": "Updated Job",
    "body": "Updated notification body"
  }
}
```

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "type": "EMAIL_NOTIFICATION",
  "status": "SCHEDULED",
  "cronExpression": "0 */10 * * * ?",
  "scheduleTime": "2025-09-28T21:00:00+05:30",
  "parameters": {
    "recipient": "admin@example.com",
    "subject": "Updated Job",
    "body": "Updated notification body"
  }
}
```

---

### 5️⃣ Delete Job
**DELETE** `/api/v1/jobs/{id}`  

#### Sample Response (200 OK)
```json
"Job with id 652f8b3d3f1e2b0012345679 deleted successfully."
```

---

### 6️⃣ Execute Job
**POST** `/api/v1/jobs/{id}/execute`  
Manually triggers the job immediately.

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "status": "EXECUTED",
  "type": "EMAIL_NOTIFICATION"
}
```

---

### 7️⃣ Rerun Job
**POST** `/api/v1/jobs/{id}/rerun`  
Creates and executes a new instance of an existing job.

#### Sample Response (200 OK)
```json
{
  "id": "652f8d5f3f1e2b0012345681",
  "type": "EMAIL_NOTIFICATION",
  "status": "SCHEDULED",
  "scheduleTime": "2025-09-28T20:10:00+05:30"
}
```

---

### 8️⃣ Cancel Job
**POST** `/api/v1/jobs/{id}/cancel`  

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "status": "CANCELLED",
  "type": "EMAIL_NOTIFICATION"
}
```

---

### 9️⃣ Reschedule Job
**PUT** `/api/v1/jobs/{id}/reschedule`  

#### Sample Request
```json
{
  "scheduleTime": "2025-09-28T22:00:00+05:30"
}
```

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "type": "EMAIL_NOTIFICATION",
  "status": "SCHEDULED",
  "scheduleTime": "2025-09-28T22:00:00+05:30"
}
```

---

### 🔟 Pause Job
**POST** `/api/v1/jobs/{id}/pause`  

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "status": "PAUSED",
  "type": "EMAIL_NOTIFICATION"
}
```

---

### 1️⃣1️⃣ Resume Job
**POST** `/api/v1/jobs/{id}/resume`  

#### Sample Response (200 OK)
```json
{
  "id": "652f8b3d3f1e2b0012345679",
  "status": "SCHEDULED",
  "type": "EMAIL_NOTIFICATION"
}
```

---

### ⚠️ Notes
* `{id}` refers to the job’s MongoDB ObjectId.  
* Status can be `SCHEDULED`, `EXECUTED`, `PAUSED`, `CANCELLED`, `COMPLETED`.  
* Errors return a JSON with an `error` field, e.g.:
```json
{
  "error": "Job not found"
}
```
* Make sure to include a valid JWT token in **all protected endpoints**.




