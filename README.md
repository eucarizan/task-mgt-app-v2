# Task Management System

A RESTful task management API built with Spring Boot and PostgreSQL.

## Features

### Authentication
- User registration with email validation
- Bearer token authentication
- Scheduled cleanup of expired tokens

### Tasks
- Create tasks with title and description
- List all task (sorted by creation date, newest first)
- Filter tasks by author
- Filter tasks by assignee
- Filter by both author and assignee
- Assign tasks to registered users
- Unassign tasks (set assignee to "none")
- Update task status (CREATED, IN_PROGRESS, COMPLETED)
- Only task author can assign tasks
- Only task author or assignee can update status

### Comments
- Add comments to any task (any logged-in user)
- List comments for a task (sorted newest first)
- View total comment count per task

## Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JDBC
- PostgreSQL
- Gradle
- JUnit 5 / Mockito / Testcontainers

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle (or use wrapper)

## Running with Docker

### 1. Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

FROM eclipse-termurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create docker-compose.yml

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/tms
      SPRING_DATASOURCE_USERNAME: tms_user
      SPRING_DATASOURCE_PASSWORD: tms_password
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: tms
      POSTGRES_USER: tms_user
      POSTGRES_PASSWORD: tms_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tms_user -d tms"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

### 3. Build and Run

```bash
# Build and start containers
docker-compose up --build

# Run in background
docker-compose up --build -d

# Stop containers
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

The API will be available at `http://localhost:8080`

## Running Locally (without Docker)

```bash
# Start PostgreSQL (required)
# Update application.properties with your database credentials

# Run the application
./gradlew bootRun

# Run tests
./gradlew test
```

## API Endpoints

### Accounts
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/accounts` | Register new user | No |

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/token` | Get bearer token | Basic |

### Tasks
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/tasks` | List all tasks | Bearer |
| GET | `/api/tasks?author={email}` | Filter by author | Bearer |
| GET | `/api/tasks?assignee={email}` | Filter by assignee | Bearer |
| POST | `/api/tasks` | Create task | Bearer |
| PUT | `/api/tasks/{id}/assign` | Assign task | Bearer |
| PUT | `/api/tasks/{id}/status` | Update status | Bearer |


### Comments 
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/tasks/{id}/comments` | List comments | Bearer |
| POST | `/api/tasks/{id}/comments` | Add comment | Bearer |

## Reques/Response Examples

### Register User
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: applicatoin/json" \
  -d '{"email": "user@example.com", "password": "sercure123"}'
```

### Get Token
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -u user@example.com:secure123
```

### Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title": "My Task", "description": "Task description"}'
```

### Add Comment
```bash
curl -X POST http://localhost:8080/api/tasks/1/comments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"text": "This is a comment"}'
```

## Future Enhancements

### Core Features
- [ ] Update task title/description
- [ ] Delete tasks
- [ ] Update/delete comments
- [ ] File attachments
- [ ] Pagination
- [ ] Filter by status
- [ ] Filter by date range
- [ ] Full-text search
- [ ] Due dates
- [ ] Task priority (LOW, MEDIUM, HIGH)
- [ ] Tags/labels
- [ ] Subtasks

## Authentication
- [ ] JWT authentication
- [ ] Refresh tokens
- [ ] Password reset

## User Managemnt
- [ ] User profiles
- [ ] User roles (ADMIN, USER)
- [ ] Teams/workspaces

## Notifications
- [ ] Email notifications
- [ ] Webhooks
- [ ] Slack/Discord integration

## Reporting
- [ ] Dashboard with statistics
- [ ] Export to CSV/PDF
- [ ] Activity feed/audit log
- [ ] Calendar sync
