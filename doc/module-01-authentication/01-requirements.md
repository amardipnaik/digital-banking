# Module 01 Authentication - Requirements

## Module Summary
Handles customer/admin authentication, password recovery, verification, JWT access, account lock policy, and login audit logging.

## Scope
- In scope: customer registration, login/logout, forgot/reset password, verification token flows, role-based access, admin enable/disable, login activity logging.
- Out of scope: KYC verification, account profile edit module, transaction authorization workflows.

## Functional Requirements
- FR-1: Customer can register with required details (name, email, mobile, password, confirm password).
- FR-2: Customer/Admin can login using email or mobile + password and receive JWT token.
- FR-3: Customer in `PENDING_VERIFICATION` status can login successfully.
- FR-4: Customer can request/confirm email and mobile verification tokens.
- FR-5: System locks account after 3 failed login attempts (`LOCKED` with lock duration).
- FR-6: `DISABLED` users cannot login.
- FR-7: Password reset flow supports forgot token + reset confirmation.
- FR-8: Admin can update user status via API (`ACTIVE` / `DISABLED`).
- FR-9: Every login attempt is stored in login activity logs with result and metadata.

## Non-Functional Requirements
- Security: BCrypt password hashing, short-lived JWT, token hash storage, OTP attempt limits.
- Validation: unique email/mobile, password confirmation, strong input validation.
- Auditability: persist login outcomes with IP, user-agent, optional device id.
- Reliability: token expiry and token reuse protection.

## User Roles
- CUSTOMER: self-register, login, verify channels, reset password.
- ADMIN: login and manage user status controls.

## Acceptance Criteria
- AC-1: Valid login returns JWT with user summary (id, role, status, verification flags).
- AC-2: Invalid credentials increment failed attempts and return auth error.
- AC-3: Third consecutive invalid password locks account.
- AC-4: `PENDING_VERIFICATION` customer login succeeds with valid credentials.
- AC-5: `DISABLED` user login is rejected.
- AC-6: Forgot/reset flow succeeds only with valid unexpired token.
- AC-7: Verification confirm updates the target channel and consumes token.
- AC-8: Admin status API accepts only `ACTIVE`/`DISABLED`.
