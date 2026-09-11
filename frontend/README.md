# Online Test Management — Frontend

Angular frontend for the **Online Test Management** MVP. The application provides separate Teacher and Student experiences and communicates with the Spring Boot backend through REST APIs. The implementation focuses on the required business flows, clear separation of concerns, role-aware navigation, and a deployment setup suitable for Docker/Dokploy.

---

## 1. Project Overview

The frontend supports the main workflows described in the assignment:

- Teacher registration and login
- Student login
- Teacher dashboard and management screens
- Student account management by teachers
- Class management and class membership
- Question management with four answer choices and tags
- Test generation from tagged questions
- Assigning tests to classes or individual students
- Student test taking and submission
- Student and Teacher result views
- Password change
- JWT-based authenticated API requests
- Role-based route protection

The UI intentionally stays lightweight because the assignment prioritizes **functional correctness and the core application flows** over advanced visual design.

---

## 2. Technology Stack

| Area | Technology |
| --- | --- |
| Framework | Angular 20 |
| Language | TypeScript 5.9 |
| Reactive / HTTP | RxJS 7 |
| Forms | Angular Reactive Forms |
| Routing | Angular Router |
| Authentication | JWT stored client-side and attached through an HTTP interceptor |
| Production web server | Nginx |
| Containerization | Docker (multi-stage build) |
| Deployment | Dokploy-compatible container deployment |

The frontend is implemented as a standalone Angular application using the modern provider-based Angular configuration style.

---

## 3. Architecture

The application is organized by responsibility rather than placing all code in a single component/service layer.

```text
src/app/
├── core/
│   ├── guards/
│   │   ├── auth.guard.ts
│   │   └── role.guard.ts
│   ├── interceptors/
│   │   └── auth.interceptor.ts
│   ├── models/
│   │   └── api.models.ts
│   └── services/
│       ├── auth.service.ts
│       ├── student-api.service.ts
│       └── teacher-api.service.ts
│
├── features/
│   ├── auth/
│   ├── student/
│   │   ├── dashboard/
│   │   ├── profile/
│   │   ├── results/
│   │   └── take-test/
│   └── teacher/
│       ├── classes/
│       ├── dashboard/
│       ├── profile/
│       ├── questions/
│       ├── results/
│       ├── students/
│       └── tests/
│
├── shared/
│   ├── format.ts
│   ├── shell.component.ts
│   └── ui.ts
│
├── app.config.ts
├── app.routes.ts
└── app.component.ts
```

### Design responsibilities

**Core**

Contains application-wide concerns such as API services, authentication state, JWT interception, route guards, and shared API models.

**Features**

Contains page-level functionality grouped by business area. Teacher and Student functionality are intentionally separated to make role-based flows easier to reason about.

**Shared**

Contains reusable UI and formatting helpers that do not belong to a single business feature.

---

## 4. Authentication and Authorization

Authentication uses the JWT returned by the backend after login or teacher registration.

The frontend handles authentication at two levels:

1. **HTTP interceptor**
   - Adds the bearer token to authenticated API requests.

2. **Route guards**
   - `authGuard` prevents unauthenticated access to protected pages.
   - `roleGuard` restricts Teacher and Student routes based on the authenticated user's role.

The frontend guards are primarily an **application UX and navigation concern**. They are not treated as a security boundary; backend authorization remains authoritative.

Example protected route structure:

```text
/teacher/*  → TEACHER only
/student/*  → STUDENT only
```

---

## 5. Main User Flows

### Teacher

```text
Register / Login
      ↓
Teacher Dashboard
      ├── Students
      ├── Classes
      ├── Questions
      ├── Tests
      ├── Results
      └── Profile
```

Teachers can create students, manage classes, manage questions/tags, generate tests, assign tests, and inspect submitted results.

### Student

```text
Login
  ↓
Student Dashboard
  ├── Available Tests
  ├── Completed Tests
  ├── Results
  └── Profile
```

A student can open an assigned test, navigate between questions, submit answers once, and view the resulting score and answer details.

---

## 6. Backend API Integration

The frontend communicates with the backend using same-origin `/api/...` paths rather than embedding a backend URL in Angular application code.

### Authentication

```text
POST /api/auth/teacher/register
POST /api/auth/teacher/login
POST /api/auth/student/login
GET  /api/auth/me
POST /api/auth/change-password
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
GET    /api/teacher/results/{id}
```

### Student APIs

```text
GET  /api/student/tests/available
GET  /api/student/tests/completed
GET  /api/student/tests/{id}
POST /api/student/tests/{id}/submit
GET  /api/student/results
GET  /api/student/results/{id}
```

The frontend service layer mirrors the backend DTO/API contract instead of duplicating business logic in the browser.

---

## 7. Local Development

### Prerequisites

- Node.js 22+
- npm
- Running instance of the Java backend
- PostgreSQL configured by the backend

### Install dependencies

```bash
npm install
```

### Start the development server

```bash
npm start
```

The application is available at:

```text
http://localhost:4200
```

The development server uses `proxy.conf.js` so browser calls to `/api/*` can be forwarded to the local backend without hard-coding the backend origin into frontend source code.

> The default development proxy is intended for local development. For production, Nginx handles `/api/*` forwarding.

---

## 8. Production Build

Build the optimized Angular application with:

```bash
npm run build
```

The production build is generated under:

```text
dist/online-test-management-frontend/browser
```

The Angular project is configured to use the production build as the default build configuration.

---

## 9. Docker Deployment

The frontend uses a multi-stage Docker build:

```text
Node.js
  ↓
Angular production build
  ↓
Nginx runtime image
```

This keeps the production image focused on serving static frontend assets.

### Build

```bash
docker build -t online-test-management-frontend .
```

### Run

The container expects `API_URL` to identify the backend upstream used by Nginx.

```bash
docker run --rm \
  -p 8080:80 \
  -e API_URL=https://api.example.com \
  online-test-management-frontend
```

The browser continues to call:

```text
/api/...
```

Nginx proxies those requests to the configured backend.

This approach keeps the backend origin out of the compiled Angular application and makes the same frontend image usable across environments by changing an environment variable at container startup.

---

## 10. Dokploy Deployment

The frontend can be deployed as a Docker application in Dokploy.

Recommended environment variable:

```env
API_URL=https://<your-backend-domain>
```

The backend URL should be reachable **from the frontend container**.

For an internal service-to-service deployment, an internal HTTP/HTTPS backend address can be used according to the Dokploy network topology. For a backend exposed with its own HTTPS domain, use that HTTPS URL.

### Reverse-proxy behavior

```text
Browser
   │
   │ HTTPS
   ▼
Frontend / Nginx
   │
   │ /api/*
   ▼
Java Backend
```

The frontend itself does not expose backend credentials or configuration to the browser.

### HTTPS and Mixed Content

When the backend is exposed over HTTPS, `API_URL` should also use `https://`.

For an HTTPS upstream, Nginx must preserve the backend hostname for TLS/SNI and the `Host` header. In addition, the public reverse proxy should pass the appropriate forwarded headers so the Java application can correctly detect the original HTTPS request.

For Spring Boot deployments behind a reverse proxy, the backend should use:

```properties
server.forward-headers-strategy=framework
```

when required by the deployment topology.

---

## 11. Environment Strategy

The frontend intentionally avoids hard-coding environment-specific API origins into application code.

### Development

```text
Angular dev server
      ↓
/api/* proxy
      ↓
Local Spring Boot backend
```

### Production

```text
Browser
      ↓
Nginx
      ↓
API_URL
      ↓
Spring Boot backend
```

This keeps deployment configuration outside the TypeScript application bundle.

---

## 12. Error Handling and UX Considerations

The frontend keeps the API layer thin and lets backend responses remain the source of truth for business validation.

Examples include:

- Invalid credentials returned by the backend
- Validation failures when creating students/questions/classes
- Unauthorized role access
- Attempting to submit a completed test again
- Loading and empty states in management screens

The application uses route protection to prevent users from navigating into incompatible areas, while backend RBAC protects the actual resources.

---

## 13. Assignment Scope

This frontend intentionally focuses on the MVP requirements.

### Implemented

- Teacher and Student authentication flows
- Role-based navigation
- Student management
- Class management
- Question/tag management
- Test generation and assignment
- Student test taking
- Results and result details
- Profile/password change
- Dockerized production serving

### Out of scope / intentionally minimal

- Advanced analytics and reporting
- Real-time features
- Email notifications
- Social login
- Password reset by email
- Advanced animations
- Complex search/filtering
- Additional question types
- Test time limits
- Multiple attempts

These boundaries keep the implementation aligned with the MVP requirements rather than adding features that were not necessary for the assignment.

---

## 14. Technical Decisions

### Same-origin `/api` calls

Using relative `/api/...` URLs keeps the Angular code independent from deployment-specific backend origins. Development uses the Angular proxy, while production uses Nginx.

### Thin frontend API services

API services are responsible for HTTP communication and DTO typing. Business rules remain on the backend so that the frontend cannot become a second source of truth.

### Route guards + backend authorization

Route guards provide a better user experience by preventing invalid navigation. They are not relied upon for security; the backend enforces role-based authorization.

### Containerized production serving

Nginx serves the generated static files efficiently and acts as the reverse proxy for `/api/*`, allowing the browser to communicate through a consistent origin.