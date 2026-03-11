# Module 01 Authentication - Database Design

## Module Summary
Authentication data model for role-based login, account verification, account lock/disable controls, and login audit tracking.

## Design Decision: How to store Admin and Customer data
Use a hybrid model:
- `users` table stores common authentication and security fields (email, mobile, password hash, lock state, status, attempts).
- `customer_profiles` table stores customer-only details.
- `admin_profiles` table stores admin-only details.
- `roles` table defines role master data, referenced by `users.role_id`.

This avoids many nullable columns in one table and keeps auth logic centralized.

## Core Tables

### 1) `roles`
Purpose: role master for authorization.

Suggested columns:
- `id` BIGINT PK (role primary key)
- `code` VARCHAR(30) UNIQUE NOT NULL (`ADMIN`, `CUSTOMER`) (stable system role key)
- `name` VARCHAR(60) NOT NULL (display name)
- `description` VARCHAR(255) (short purpose of role)
- `is_active` BOOLEAN NOT NULL DEFAULT TRUE (soft enable/disable role)
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP (record creation time)
- `updated_at` TIMESTAMP (last update time)

### 2) `users`
Purpose: one row per login identity (admin or customer).

Suggested columns:
- `id` BIGINT PK (user primary key)
- `role_id` BIGINT NOT NULL FK -> `roles.id` (current role assigned to user)
- `email` VARCHAR(150) UNIQUE NOT NULL (primary login email)
- `mobile_number` VARCHAR(20) UNIQUE NOT NULL (primary login mobile)
- `password_hash` VARCHAR(255) NOT NULL (BCrypt/Argon hashed password only)
- `email_verified` BOOLEAN NOT NULL DEFAULT FALSE (email verification flag)
- `email_verified_at` TIMESTAMP NULL (when email was verified)
- `mobile_verified` BOOLEAN NOT NULL DEFAULT FALSE (mobile verification flag)
- `mobile_verified_at` TIMESTAMP NULL (when mobile was verified)
- `failed_login_attempts` SMALLINT NOT NULL DEFAULT 0 (consecutive failed login count)
- `last_failed_login_at` TIMESTAMP NULL (latest failed login timestamp)
- `lock_until` TIMESTAMP NULL (temporary lock expiry timestamp)
- `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION'
  - Allowed values: `PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED`
- `disabled_reason` VARCHAR(255) NULL (admin-entered disable reason)
- `disabled_by` BIGINT NULL FK -> `users.id` (admin user id who disabled account)
- `disabled_at` TIMESTAMP NULL (when account was disabled)
- `last_login_at` TIMESTAMP NULL (last successful login time)
- `last_login_ip` VARCHAR(45) NULL (IP used in last successful login)
- `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE (soft delete marker)
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP (record creation time)
- `updated_at` TIMESTAMP (last update time)

### 3) `customer_profiles`
Purpose: customer-specific data, 1:1 with `users`.

Suggested columns:
- `id` BIGINT PK (customer profile primary key)
- `user_id` BIGINT UNIQUE NOT NULL FK -> `users.id` (1:1 reference to login user)
- `full_name` VARCHAR(120) NOT NULL (customer full legal name)
- `date_of_birth` DATE NULL (DOB for identity checks)
- `address_line_1` VARCHAR(150) NULL (primary address line)
- `address_line_2` VARCHAR(150) NULL (secondary address line)
- `city` VARCHAR(80) NULL (city name)
- `state` VARCHAR(80) NULL (state/province name)
- `postal_code` VARCHAR(20) NULL (ZIP/postal code)
- `country` VARCHAR(80) NULL (country name)
- `kyc_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' (KYC workflow state)
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP (record creation time)
- `updated_at` TIMESTAMP (last update time)

### 4) `admin_profiles`
Purpose: admin-specific data, 1:1 with `users`.

Suggested columns:
- `id` BIGINT PK (admin profile primary key)
- `user_id` BIGINT UNIQUE NOT NULL FK -> `users.id` (1:1 reference to login user)
- `full_name` VARCHAR(120) NOT NULL (admin full name)
- `employee_code` VARCHAR(40) UNIQUE NOT NULL (internal employee identifier)
- `department` VARCHAR(80) NULL (admin department/team)
- `designation` VARCHAR(80) NULL (admin role title)
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP (record creation time)
- `updated_at` TIMESTAMP (last update time)

### 5) `login_activity_logs`
Purpose: audit every login attempt (success and failure).

Suggested columns:
- `id` BIGINT PK (activity log primary key)
- `user_id` BIGINT NULL FK -> `users.id` (null if email/mobile not found)
- `login_identifier` VARCHAR(150) NOT NULL (email/mobile used for login)
- `attempt_no` SMALLINT NULL (attempt sequence for that login window)
- `result` VARCHAR(20) NOT NULL
  - Allowed values: `SUCCESS`, `FAILURE`, `BLOCKED`, `DISABLED`
- `failure_reason` VARCHAR(120) NULL
  - Examples: `INVALID_PASSWORD`, `ACCOUNT_LOCKED`, `ACCOUNT_DISABLED`, `EMAIL_NOT_VERIFIED`, `MOBILE_NOT_VERIFIED`
- `ip_address` VARCHAR(45) NULL (IPv4/IPv6 source address)
- `user_agent` VARCHAR(500) NULL (browser/app and OS details from request header)
- `device_id` VARCHAR(120) NULL (client device identifier if provided)
- `location_hint` VARCHAR(120) NULL (derived city/region hint from IP or device data)
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP (attempt event time)

### 6) `auth_tokens`
Purpose: unified token store for email/mobile verification and password reset.

Suggested columns:
- `id` BIGINT PK (token primary key)
- `user_id` BIGINT NOT NULL FK -> `users.id` (token owner)
- `token_type` VARCHAR(30) NOT NULL
  - Allowed values: `EMAIL_VERIFY`, `MOBILE_VERIFY`, `PASSWORD_RESET`
- `channel` VARCHAR(20) NULL
  - Allowed values when applicable: `EMAIL`, `MOBILE`
- `token_hash` VARCHAR(255) UNIQUE NOT NULL (store only hashed token/OTP)
- `expires_at` TIMESTAMP NOT NULL (token validity end time)
- `consumed_at` TIMESTAMP NULL (when token was successfully used)
- `attempt_count` SMALLINT NOT NULL DEFAULT 0 (wrong verification attempts used)
- `max_attempts` SMALLINT NOT NULL DEFAULT 3 (allowed attempts before invalidation)
- `is_used` BOOLEAN NOT NULL DEFAULT FALSE (token already consumed flag)
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP (token issue time)

## Indexing Plan
- `users(email)` UNIQUE
- `users(mobile_number)` UNIQUE
- `users(role_id, status)`
- `users(lock_until)`
- `login_activity_logs(user_id, created_at DESC)`
- `login_activity_logs(login_identifier, created_at DESC)`
- `auth_tokens(user_id, token_type, expires_at DESC)`
- `auth_tokens(channel, token_type, expires_at DESC)`
- Unique active token rule:
  - enforce one active token per `user_id + token_type + channel`
  - implementation idea (PostgreSQL): partial unique index where `is_used = FALSE` and `expires_at > now()`

## Login Attempt and Disable/Enable Rules
- Max failed login attempts: `3`.
- On each invalid password:
  - increment `users.failed_login_attempts`
  - insert `login_activity_logs` with `result='FAILURE'`
- When attempts reach 3:
  - set `users.status='LOCKED'`
  - set `users.lock_until` (example: current time + 30 minutes)
  - insert `login_activity_logs` with `result='BLOCKED'`
- On successful login:
  - allow only if `status='ACTIVE'` and not locked by time
  - reset `failed_login_attempts` to `0`
  - clear `last_failed_login_at`
  - update `last_login_at` and `last_login_ip`
  - insert `login_activity_logs` with `result='SUCCESS'`
- `DISABLED` means hard block by admin until manually enabled.
- Enable flow by admin:
  - set `status='ACTIVE'`
  - clear `disabled_reason`, `disabled_by`, `disabled_at`
  - reset lock and failed attempts fields

## Verification Rules
- Customer can login only when both `email_verified = TRUE` and `mobile_verified = TRUE`.
- Before both verifications are complete, keep status as `PENDING_VERIFICATION`.
- Admin users may be seeded as verified and `ACTIVE` by default.
- Verification tokens are stored in `auth_tokens` with `token_type` as `EMAIL_VERIFY` or `MOBILE_VERIFY`.
- Password reset tokens are stored in `auth_tokens` with `token_type` as `PASSWORD_RESET`.

## Recommended Constraints
- FK: `users.role_id -> roles.id`
- FK: profile tables `user_id -> users.id` with `UNIQUE` for 1:1 mapping
- CHECK: `failed_login_attempts >= 0 AND failed_login_attempts <= 3`
- CHECK: `status IN ('PENDING_VERIFICATION','ACTIVE','LOCKED','DISABLED')`
- CHECK: `result IN ('SUCCESS','FAILURE','BLOCKED','DISABLED')`
- CHECK: `token_type IN ('EMAIL_VERIFY','MOBILE_VERIFY','PASSWORD_RESET')`
- CHECK: `channel IN ('EMAIL','MOBILE') OR channel IS NULL`
- CHECK: token/channel consistency:
  - `token_type = 'PASSWORD_RESET'` => `channel IS NULL`
  - `token_type IN ('EMAIL_VERIFY','MOBILE_VERIFY')` => `channel IS NOT NULL`
- CHECK: `attempt_count >= 0 AND max_attempts >= 1`

## Migration Notes
- Version: `auth-v2`.
- Migration path from v1:
  - create `roles`, `customer_profiles`, `admin_profiles`, `login_activity_logs`, `auth_tokens`
  - move existing `users.role` enum to `users.role_id` FK
  - migrate old `verification_tokens` and `password_reset_tokens` data into `auth_tokens` using `token_type`
  - backfill `roles` with `ADMIN` and `CUSTOMER`
