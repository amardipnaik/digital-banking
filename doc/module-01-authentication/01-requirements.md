# Module 01 Authentication - Requirements

## Module Summary
Handles login, registration, password reset, and role-based access for customer and admin users.

## Scope
- In scope: register customer, login/logout, forgot/reset password, JWT auth, role-based access control.
- Out of scope: profile management and customer KYC workflows.

## Functional Requirements
- FR-1: Customer can register with required personal details.
- FR-2: Customer/Admin can login and receive JWT token.
- FR-3: Customer/Admin can initiate and complete password reset with token.
- FR-4: Protected APIs require valid JWT and role authorization.
- FR-5: Customer login is allowed only after both email and mobile verification.
- FR-6: After 3 failed login attempts, account is locked as per lock policy.
- FR-7: Admin can disable or re-enable user accounts.

## Non-Functional Requirements
- Performance: auth APIs should meet project response-time targets.
- Security: BCrypt password hashing, JWT expiry, secure token validation.
- Validation: strong password, unique email/phone, required fields.
- Logging/Monitoring: audit login success/failure and reset attempts.
- Audit: capture all login attempts with IP, user-agent, result, and reason.

## User Roles
- CUSTOMER: self registration/login/password reset.
- ADMIN: login/password reset (pre-configured account).

## Acceptance Criteria
- AC-1: Valid login returns JWT, role, expiry, and user identity.
- AC-2: Invalid credentials return clear auth error.
- AC-3: Password reset token cannot be reused after success/expiry.
- AC-4: User is locked after 3 consecutive failed attempts.
- AC-5: Disabled user cannot log in until admin enables the account.
- AC-6: Customer cannot log in before email and mobile are verified.
