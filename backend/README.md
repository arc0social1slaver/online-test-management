# Online Test Management — Backend

A RESTful backend for an Online Test Management MVP. The application supports two roles — **Teacher** and **Student** — and covers the core workflow from question/test management through test assignment, submission, scoring, and result review.

## 1. Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Web | Spring MVC / REST API |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16+ |
| Validation | Jakarta Bean Validation |
| Password hashing | BCrypt |
| Build tool | Maven |
| Integration tests | Spring Boot Test + MockMvc + Testcontainers |
| Containerization | Docker / Docker Compose |

## 2. What This Backend Implements

The backend implements the main MVP flows from the assignment:

### Authentication & users

- Teacher registration with email/password.
- Teacher login.
- Student accounts are created by Teachers.
- Student login with username/password.
- Change password for authenticated users.
- JWT-based stateless authentication.
- BCrypt password hashing.

### Question management

Teachers can:

- Create questions.
- View questions.
- Edit questions.
- Delete questions.
- Attach one or more tags to a question.

Each question contains four answer choices and one correct answer.

### Class management

Teachers can:

- Create, edit, and delete classes.
- Add/remove students from classes.
- View students in a class.

A student can belong to multiple classes.

### Test management

Teachers can:

- Generate a test from one or more question tags.
- Specify the requested number of questions.
- Assign a generated test directly to a student or to a class.
- View tests they created.

When a test is generated, its selected questions are fixed. The requested number of questions cannot exceed the number of matching available questions.

### Test taking & results

Students can:

- View available tests.
- Open an assigned test.
- Answer four-choice multiple-choice questions.
- Submit a test once.
- View completed tests and their own results.

When a test is submitted, answers are saved, the score is calculated automatically, and the result is persisted.

Results retain the information required to review the submission even if the student later loses access to the test.

## 3. Authorization Model

Authorization is enforced by the backend, not only by the frontend.

- `TEACHER` can access `/api/teacher/**`.
- `STUDENT` can access `/api/student/**`.
- Authentication endpoints under `/api/auth/**` are public.
- Students can only access their own results.
- Teachers can only manage resources belonging to them.
- Student-facing test data does not expose the correct answer before submission.

JWT is sent using:

```http
Authorization: Bearer <JWT>
```

## 4. Project Structure

```text
src/main/java/com/example/OnlineTestManagement
├── config/         # Application/data initialization
├── controller/     # REST controllers
├── dto/            # Request/response DTOs
├── entity/         # JPA entities
├── exception/      # API exceptions and global error handling
├── repository/     # Spring Data repositories
├── security/       # JWT authentication and Spring Security config
└── service/        # Application/business logic
```

The project follows a simple layered architecture:

```text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

DTOs are used at the API boundary instead of exposing JPA entities directly.

## 5. REST API Overview

### Authentication

```text
POST /api/auth/teacher/register
POST /api/auth/teacher/login
POST /api/auth/student/login
POST /api/auth/change-password
GET  /api/auth/me
```

### Teacher APIs

```text
GET    /api/teacher/students
POST   /api/teacher/students

GET    /api/teacher/tags
POST   /api/teacher/tags

GET    /api/teacher/questions
POST   /api/teacher/questions
PUT    /api/teacher/questions/{id}
DELETE /api/teacher/questions/{id}

GET    /api/teacher/classes
GET    /api/teacher/classes/{id}
POST   /api/teacher/classes
PUT    /api/teacher/classes/{id}
DELETE /api/teacher/classes/{id}
POST   /api/teacher/classes/{classId}/students/{studentId}
DELETE /api/teacher/classes/{classId}/students/{studentId}

GET    /api/teacher/tests
POST   /api/teacher/tests
POST   /api/teacher/tests/{testId}/assign

GET    /api/teacher/results
GET    /api/teacher/results/{resultId}
```

### Student APIs

```text
GET  /api/student/tests/available
GET  /api/student/tests/completed
GET  /api/student/tests/{testId}
POST /api/student/tests/{testId}/submit
GET  /api/student/results
GET  /api/student/results/{resultId}
```

## 6. Configuration

The application reads its database and JWT settings from environment variables.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/online_test` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `PORT` | `8080` | Application port |
| `JWT_SECRET` | development placeholder | JWT signing secret |
| `JWT_EXPIRATION_MS` | `86400000` | JWT lifetime in milliseconds |

For deployment, replace the development JWT secret with a strong random secret and manage credentials through the hosting platform's secret/environment configuration.

## 7. Run Locally

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 16+ (when not using Docker)

### Setup

```bash
docker compose up -d
```

Then start Spring Boot:

```bash
cd backend/
mvn spring-boot:run
```

Default local URL:

```text
http://localhost:8080
```

Swagger UI is available at: `http://localhost:8000/swagger-ui/index.html`

## 8. Integration Testing

The project includes integration tests that exercise the application through the HTTP layer and a real PostgreSQL database.

The test stack uses:

- Spring Boot test support.
- MockMvc for HTTP requests.
- Spring Security/JWT filter chain.
- JPA/Hibernate persistence.
- Testcontainers PostgreSQL.

The tests cover the main business flow, including:

- Teacher authentication.
- Role-based authorization.
- Student creation.
- Question creation.
- Test generation.
- Test assignment.
- Student access to assigned tests.
- Invalid/incomplete submissions.
- Successful submission and score calculation.
- Preventing a second attempt.
- Result access for the correct user/role.

### Run tests

```bash
mvn test
```

Docker must be available because Testcontainers starts an isolated PostgreSQL container for the integration test suite.

The test database is independent from the development/production PostgreSQL instance.

## 9. Dockerfile

The backend uses a multi-stage Docker build:

1. Maven + JDK 21 image builds the application.
2. A lightweight Eclipse Temurin JRE 21 image runs the packaged JAR.
3. The runtime container runs as a non-root user.

Build the image manually:

```bash
docker build -t online-test-management-backend .
```

Run it by providing the database/JWT settings through environment variables:

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/online_test" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="postgres" \
  -e JWT_SECRET="replace-with-a-strong-secret" \
  online-test-management-backend
```

For normal development, Docker Compose is simpler because it also provisions PostgreSQL.

## 10. Deployment

The backend is container-ready and can be deployed to a platform such as Dokploy.

For a typical deployment:

1. Build the application from the repository using the provided `Dockerfile`.
2. Provide `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` through the platform environment variables.
3. Configure `JWT_SECRET` with a strong production secret.
4. Expose the application port using `PORT` (default `8080`).
5. Use the PostgreSQL service's internal connection hostname when backend and database share the same container network.
6. When the application is behind an HTTPS reverse proxy, enable forwarded-header handling so generated links such as Swagger use the external HTTPS scheme.

For example:

```properties
server.forward-headers-strategy=framework
```

## 11. Development Notes

### Database schema

The MVP uses:

```
spring.jpa.hibernate.ddl-auto=update
```

This is intentional for a small assignment/MVP to keep local setup simple. For a production system, schema changes should be managed with a migration tool such as Flyway or Liquibase.

### Result persistence

The result model stores enough submission information to support result review after the student's access to the original test changes. This matches the assignment requirement that completed results remain available.

### Question matching

When generating a test from multiple tags, a question matches when it contains at least one selected tag.

## 12. MVP Scope / Out of Scope

The implementation focuses on the required four-choice multiple-choice MVP.

The assignment intentionally leaves the following out of scope:

- Email verification.
- Email-based password reset.
- Email notifications.
- Social login.
- Advanced reporting/statistics.
- Question types other than four-choice multiple choice.
- Question images/files.
- Test time limits.
- Multiple attempts.
- Automatic test expiration.
- Real-time functionality.
- Complex search/filtering.
- Production deployment infrastructure beyond basic containerization.

## 13. Engineering Decisions

A few implementation choices are intentionally simple and interview-assignment friendly:

- **JWT + stateless Spring Security** keeps authentication straightforward for an SPA frontend.
- **DTOs** separate HTTP contracts from database entities.
- **Service layer** centralizes business rules and ownership checks.
- **PostgreSQL** is used as the real relational database in both development and integration tests.
- **Testcontainers** avoids depending on a shared test database.
- **Docker multi-stage build** keeps the final runtime image smaller and avoids shipping Maven tooling into production.
- **Environment-based configuration** keeps database credentials and deployment-specific settings outside source code.
