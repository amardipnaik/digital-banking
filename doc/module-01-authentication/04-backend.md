# Module 01 Authentication - Backend Design

## Module Summary
Spring Boot auth module with JWT security, login audit logs, token-based verification/reset, and admin status controls.

## Controllers
- `AuthController` (`/api/auth`)
  - register, login, verification request/confirm, forgot/reset password, logout, me
- `AdminAuthController` (`/api/admin/auth/users`)
  - user status update endpoint

## Core Service Responsibilities
- Register customer with `PENDING_VERIFICATION` status.
- Login with email or mobile identifier.
- Enforce failed login policy (max 3, then lock).
- Allow login for `ACTIVE` and `PENDING_VERIFICATION`.
- Reject login for `DISABLED` and currently locked users.
- Generate and validate OTP/reset tokens via `auth_tokens`.
- Maintain login activity audit trail.

## Security Model
- JWT bearer auth for protected APIs.
- Public APIs: register/login/verification/forgot/reset and health/swagger endpoints.
- Admin APIs require `ADMIN` role.
- Password hashing: BCrypt.

## Persistence Model (Module 1)
- `users` with role/status/verification flags and lock metadata.
- `roles` for role codes (`CUSTOMER`, `ADMIN`).
- `customer_profiles` and `admin_profiles`.
- `auth_tokens` for email/mobile verification + password reset.
- `login_activity_logs` for every login attempt.

## Important Backend Rules
- Third failed login sets `LOCKED` status and lock duration.
- `DISABLED` users are blocked until admin sets `ACTIVE`.
- Verification confirm updates channel flags and token usage.
- Password reset consumes reset token and clears lock/failure counters.
