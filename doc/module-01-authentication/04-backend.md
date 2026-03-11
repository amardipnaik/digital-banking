# Module 01 Authentication - Backend Design

## Module Summary
Spring Boot auth service responsible for identity lifecycle and JWT security.

## Service Responsibilities
- Register customer users.
- Authenticate credentials and issue JWT.
- Handle logout behavior and reset-password workflow.

## Controllers
- `AuthController` under `/api/auth`

## DTOs / Models
- Request DTOs: login, register, forgot/reset password.
- Response DTOs: JWT response with token metadata and role.

## Validation and Error Handling
- Bean validation on incoming payloads.
- Consistent error response for invalid credentials, duplicate user, expired token.

## Security / Authorization
- `/api/auth/**` public where needed.
- Other APIs protected via JWT filter.
- Role-aware authorization checks.
