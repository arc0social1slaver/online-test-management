# Online Test Management

A full-stack web application for managing questions, classes, tests, student assignments, test submissions, and results. The implementation focuses on the core business flows, clear separation of responsibilities, backend-enforced authorization, REST API integration, automated integration testing, and containerized deployment.

## Live Demo

**Frontend:** [https://online-test-management-pyiblk-2b6990-187-77-153-25.sslip.io](https://online-test-management-pyiblk-2b6990-187-77-153-25.sslip.io)

> The deployment is intended for demonstration purposes

## Overview

The system has two roles:

- **Teacher** — manages students, classes, questions, tags, tests, assignments, and results.
- **Student** — views assigned tests, takes each test once, submits answers, and views personal results.

The main business flow is:

```text
Teacher creates questions
        ↓
Teacher generates a test from question tags
        ↓
Teacher assigns the test to a class or student
        ↓
Student opens and completes the test
        ↓
Student submits answers
        ↓
Backend calculates and persists the score
        ↓
Student / Teacher reviews the result
```

## Requirements Completion Checklist

### Core MVP

- [x] Teacher registration and login
- [x] Student accounts created by teachers
- [x] Student login
- [x] Password change for authenticated users
- [x] Question CRUD with four answer choices, one correct answer, and tags
- [x] Default grade tags and custom tags
- [x] Class CRUD and student membership management
- [x] Test generation from selected question tags
- [x] Fixed question set after a test is created
- [x] Assign tests to a class or an individual student
- [x] Student dashboard with available/completed tests
- [x] Four-choice test taking flow
- [x] One submission attempt per assigned test
- [x] Automatic score calculation
- [x] Persistent test results
- [x] Teacher result review
- [x] Student can view only personal results
- [x] Backend-enforced role-based authorization

### Engineering / Delivery

- [x] REST API
- [x] JWT authentication
- [x] BCrypt password hashing
- [x] PostgreSQL persistence
- [x] DTO-based API boundary
- [x] Integration tests using Testcontainers PostgreSQL
- [x] Dockerized backend
- [x] Dockerized Angular frontend with Nginx
- [x] Deployment on Dokploy
- [x] HTTPS-ready reverse-proxy configuration

## Technology Stack

### Backend

- **Java 21**
- **Spring Boot 3.4.5**
- Spring MVC / REST
- Spring Security
- JWT (`jjwt`)
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Jakarta Bean Validation
- BCrypt
- Maven
- Spring Boot Test / MockMvc
- Testcontainers

### Frontend

- **Angular 20**
- TypeScript 5.9
- Angular Router
- Angular Reactive Forms
- RxJS
- Angular CLI

### DevOps

- Docker
- Docker Compose
- Nginx
- Dokploy
- HTTPS reverse proxy

## Repository Structure

```text
.
├── backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   │   └── app/
│   ├── Dockerfile
│   ├── nginx/
│   ├── package.json
│   ├── angular.json
│   └── README.md
├── docker-compose.yml
└── README.md
```

The application is intentionally split into two projects so that frontend concerns, backend business rules, and persistence concerns remain independently testable and deployable.

## Architecture

### Backend

The backend uses a simple layered architecture:

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

Supporting layers include:

```text
security/   → JWT authentication and Spring Security
            
DTOs        → API request/response contracts

exception/  → centralized API error handling

entity/     → JPA persistence model
```

The backend does not rely on frontend authorization. Role-based access and ownership rules are enforced on the server.

### Frontend

The Angular application is organized by responsibility and feature:

```text
src/app/
├── core/            # auth service, guards, interceptor, API models/services
├── features/
│   ├── auth/
│   ├── teacher/
│   └── student/
└── shared/          # reusable shell/UI/formatting helpers
```

The frontend uses relative `/api/...` URLs. Local development uses the Angular dev proxy, while the production container uses Nginx to forward `/api/*` to the backend.

## Separate Documentation

Detailed component-level documentation is maintained with each application:

- [Backend README](backend/README.md)
- [Frontend README](frontend/README.md)
- [API Documentation / Swagger](https://tmapv-backend-w9aaqs-4d8fc7-187-77-153-25.sslip.io/swagger-ui/index.html)