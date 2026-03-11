# Authentication Module — BRD

> **Project:** Digital Banking Platform | **Module:** 1 — Authentication
> **Version:** 1.0 | **Date:** 2026-03-11 | **Status:** Draft

---

## 1. Overview

The Authentication Module is the security gateway for the platform. It handles:

- User registration (Customer self-service; Admin pre-seeded)
- Email verification (token link sent post-registration)
- Mobile number verification (OTP sent via SMS post-registration)
- Login / Logout with JWT
- Password reset via email token
- Role-Based Access Control (ADMIN / CUSTOMER)
- Audit logging of all auth events

**Out of scope:** OAuth2 / social login, 2FA, biometrics, SMTP/SMS provider setup.

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18+ (React Router v6, Axios) |
| Backend | Spring Boot 3.x, Java 17+ |
| Database | MySQL 8.x / PostgreSQL 15+ |
| Auth | JWT (HS256) + Spring Security 6.x |
| Password Hash | BCrypt (strength 12) |
| Build / Docs | Maven, Swagger / OpenAPI 3.0 |

---

## 3. Roles

| Role | How Created | Access |
|---|---|---|
| `CUSTOMER` | Self-registration via `/api/auth/register` | Own account, transactions, balance |
| `ADMIN` | Pre-seeded at system setup (no public endpoint) | All customer management, admin panels |

---

## 4. Database Design

> Full DDL → `docs/sql/auth-module-schema.sql`

### Tables Overview

| Table | Purpose | Key Relations |
|---|---|---|
| `roles` | Master role list (`ADMIN`, `CUSTOMER`) | Referenced by `user_roles` |
| `users` | Auth credentials + account status | Parent of all other tables |
| `profiles` | Personal / KYC details (1:1 with users) | FK → `users.id` |
| `user_roles` | Junction: user ↔ role (M:M) | FK → `users.id`, `roles.id` |
| `verification_tokens` | Email link & mobile OTP tokens (15-min TTL) | FK → `users.id` |
| `password_reset_tokens` | Single-use email reset tokens (30-min TTL) | FK → `users.id` |
| `activity_log` | Immutable audit trail of auth events | FK → `users.id` |

### `roles`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `role_name` | VARCHAR(50) | UNIQUE — `ADMIN`, `CUSTOMER` |
| `description` | VARCHAR(255) | Optional label |
| `created_at` | TIMESTAMP | Default now |

### `users`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `email` | VARCHAR(150) | UNIQUE, indexed, login key |
| `password` | VARCHAR(255) | BCrypt hash only |
| `is_active` | BOOLEAN | Default FALSE; set TRUE after both verifications pass |
| `is_locked` | BOOLEAN | Default FALSE; set after 5 failed logins |
| `failed_login_attempts` | INT | Default 0; reset on success |
| `lock_time` | TIMESTAMP | Nullable; set when locked |
| `last_login_at` | TIMESTAMP | Nullable; updated on each login |
| `email_verified` | BOOLEAN | Default FALSE; set TRUE after email link clicked |
| `phone_verified` | BOOLEAN | Default FALSE; set TRUE after OTP confirmed |
| `created_at` / `updated_at` | TIMESTAMP | Auto-managed |

> Personal data (name, phone, address) lives in `profiles` — keeps auth layer lean.
> Account is only fully active (`is_active = TRUE`) once **both** email and phone are verified.

### `profiles`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT FK | UNIQUE — enforces 1:1 with `users` |
| `full_name` | VARCHAR(100) | Required |
| `phone_number` | VARCHAR(15) | UNIQUE, indexed |
| `date_of_birth` | DATE | Required (must be ≥18) |
| `gender` | ENUM | `MALE`, `FEMALE`, `OTHER` — nullable |
| `address`, `city`, `state`, `pincode` | TEXT / VARCHAR | Nullable |
| `profile_pic` | VARCHAR(500) | URL — nullable |
| `created_at` / `updated_at` | TIMESTAMP | Auto-managed |

### `user_roles`

| Column | Type | Notes |
|---|---|---|
| `user_id` | BIGINT FK | Composite PK with `role_id` |
| `role_id` | BIGINT FK | |
| `assigned_at` | TIMESTAMP | Default now |

### `verification_tokens`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT FK | |
| `token` | VARCHAR(255) | UUID (email link) or 6-digit numeric string (mobile OTP) |
| `type` | ENUM | `EMAIL` or `MOBILE` |
| `expiry_date` | TIMESTAMP | NOW() + 15 min |
| `is_used` | BOOLEAN | Default FALSE; set TRUE after successful verification |
| `created_at` | TIMESTAMP | Default now |

### `password_reset_tokens`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT FK | |
| `token` | VARCHAR(255) | UNIQUE, indexed — UUID |
| `expiry_date` | TIMESTAMP | NOW() + 30 min |
| `is_used` | BOOLEAN | Default FALSE; set TRUE after use |
| `created_at` | TIMESTAMP | Default now |

### `activity_log`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT FK | Indexed |
| `activity_type` | ENUM | See values below |
| `status` | ENUM | `SUCCESS` / `FAILURE` |
| `ip_address` | VARCHAR(50) | Nullable |
| `user_agent` | VARCHAR(500) | Nullable |
| `description` | VARCHAR(500) | Nullable |
| `created_at` | TIMESTAMP | Indexed; default now |

**`activity_type` values:** `LOGIN`, `LOGOUT`, `REGISTER`, `EMAIL_VERIFICATION_SENT`, `EMAIL_VERIFIED`, `MOBILE_OTP_SENT`, `MOBILE_VERIFIED`, `PASSWORD_RESET_REQUEST`, `PASSWORD_RESET`, `PASSWORD_CHANGE`, `ACCOUNT_LOCKED`, `ACCOUNT_UNLOCKED`, `TOKEN_REFRESH`

### ER Diagram

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : "has"
    ROLES ||--o{ USER_ROLES : "assigned to"
    USERS ||--|| PROFILES : "has one"
    USERS ||--o{ VERIFICATION_TOKENS : "verifies via"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"
    USERS ||--o{ ACTIVITY_LOG : "generates"

    USERS { bigint id PK; varchar email; varchar password; boolean is_active; boolean is_locked; boolean email_verified; boolean phone_verified; int failed_login_attempts }
    ROLES { bigint id PK; varchar role_name; varchar description }
    PROFILES { bigint id PK; bigint user_id FK; varchar full_name; varchar phone_number; date date_of_birth }
    USER_ROLES { bigint user_id FK; bigint role_id FK; timestamp assigned_at }
    VERIFICATION_TOKENS { bigint id PK; bigint user_id FK; varchar token; enum type; timestamp expiry_date; boolean is_used }
    PASSWORD_RESET_TOKENS { bigint id PK; bigint user_id FK; varchar token; timestamp expiry_date; boolean is_used }
    ACTIVITY_LOG { bigint id PK; bigint user_id FK; enum activity_type; enum status; timestamp created_at }
```

---

## 5. Feature Requirements

### Common Patterns

Every feature follows this convention:
- Backend always logs to `activity_log` with `SUCCESS` or `FAILURE`
- All protected endpoints require a valid JWT in `Authorization: Bearer <token>`
- Validation errors return `400`; constraint violations return `409`

---

### 5.1 Registration

**Actor:** New Customer (unauthenticated) | **Endpoint:** `POST /api/auth/register`

**Flow:**
1. Customer submits form → Frontend validates → API called
2. Backend checks email + phone uniqueness → hashes password (BCrypt)
3. Creates `users` (with `is_active=FALSE`) + `profiles` records → assigns `CUSTOMER` role in `user_roles`
4. Generates EMAIL token → stores in `verification_tokens` → sends verification email
5. Generates MOBILE OTP → stores in `verification_tokens` → sends OTP via SMS
6. Logs `REGISTER / SUCCESS`, `EMAIL_VERIFICATION_SENT / SUCCESS`, `MOBILE_OTP_SENT / SUCCESS` → returns `201`
7. Frontend shows: "Check your email and mobile to verify your account"

**Error Flows:**

| Scenario | HTTP | Message |
|---|---|---|
| Duplicate email | 409 | "Email already registered" |
| Duplicate phone | 409 | "Phone number already in use" |
| Weak password | 400 | Password policy error |
| Passwords don't match | — | Caught client-side |

**Acceptance Criteria:**
- [ ] User created with `is_active=FALSE`, `email_verified=FALSE`, `phone_verified=FALSE`, password BCrypt-hashed
- [ ] Profile record and `CUSTOMER` role created in same transaction
- [ ] EMAIL token (UUID) and MOBILE OTP (6-digit) stored in `verification_tokens` with 15-min TTL
- [ ] `REGISTER / SUCCESS` logged in `activity_log`

---

### 5.2 Email & Mobile Verification

**Actor:** New Customer (unauthenticated)

**Email Verification — `GET /api/auth/verify-email?token=TOKEN`**
1. User clicks link in email → Backend validates token (exists, type=EMAIL, not expired, not used)
2. Sets `users.email_verified=TRUE`, marks token `is_used=TRUE`
3. If `phone_verified` is also TRUE → sets `is_active=TRUE`
4. Logs `EMAIL_VERIFIED / SUCCESS` → returns `200`
5. Frontend shows: "Email verified!" — prompts to complete mobile verification if pending

**Mobile OTP Verification — `POST /api/auth/verify-mobile`**
1. User enters 6-digit OTP → Backend validates token (exists, type=MOBILE, not expired, not used)
2. Sets `users.phone_verified=TRUE`, marks token `is_used=TRUE`
3. If `email_verified` is also TRUE → sets `is_active=TRUE`
4. Logs `MOBILE_VERIFIED / SUCCESS` → returns `200`
5. Frontend redirects to `/login` with: "Account activated! Please log in."

**Resend — `POST /api/auth/resend-verification`**
- Body: `{ "type": "EMAIL" | "MOBILE", "identifier": "<email or phone>" }`
- Invalidates existing unused tokens for that user+type, generates fresh token/OTP, re-sends
- Rate-limited: max 3 resends per hour per user

**Error Flows:**

| Scenario | HTTP | Message |
|---|---|---|
| Token expired | 400 | "Verification link/OTP has expired. Request a new one." |
| Token already used | 400 | "Already verified." |
| Invalid OTP | 400 | "Incorrect OTP." |
| Resend limit exceeded | 429 | "Too many requests. Try again later." |

**Acceptance Criteria:**
- [ ] `is_active` set to TRUE only when **both** `email_verified=TRUE` AND `phone_verified=TRUE`
- [ ] Login blocked with `403` if account is not yet active (pending verification)
- [ ] Token TTL is 15 minutes; single-use
- [ ] `EMAIL_VERIFIED` and `MOBILE_VERIFIED` logged in `activity_log`
- [ ] Resend invalidates previous token before issuing a new one

---

### 5.3 Login

**Actor:** Any user | **Endpoint:** `POST /api/auth/login`

**Flow:**
1. Frontend sends credentials → Backend looks up user by email
2. Validates BCrypt password + checks `is_active` / `is_locked`
3. Resets `failed_login_attempts=0`, updates `last_login_at`
4. Generates JWT access + refresh tokens → Logs `LOGIN / SUCCESS` → returns `200`
5. Frontend stores token; redirects to role dashboard

**Error Flows:**

| Scenario | HTTP | Message |
|---|---|---|
| Email not found | 401 | "Invalid email or password" (generic) |
| Wrong password (attempts 1–4) | 401 | "Invalid email or password (X left)" |
| Wrong password (attempt 5) | 401 | "Account locked" — sets `is_locked=TRUE`, logs `ACCOUNT_LOCKED` |
| Already locked | 423 | "Account is locked" |
| Email/phone not verified | 403 | "Please verify your email and mobile number to activate your account." |
| Inactive account (admin disabled) | 403 | "Account deactivated. Contact support." |

**Acceptance Criteria:**
- [ ] `200` returns access token + refresh token with role and userId
- [ ] `failed_login_attempts` increments on failure; resets on success
- [ ] Account locks at 5 failures; returns `423` until unlocked
- [ ] `LOGIN / SUCCESS` or `LOGIN / FAILURE` always logged

---

### 5.4 Logout

**Actor:** Authenticated user | **Endpoint:** `POST /api/auth/logout` *(JWT required)*

**Flow:**
1. Frontend sends request with JWT → Backend blacklists the token (Redis / in-memory)
2. Logs `LOGOUT / SUCCESS` → returns `200`
3. Frontend clears stored tokens → redirects to `/login`

**Acceptance Criteria:**
- [ ] Blacklisted token rejected on all subsequent requests
- [ ] `LOGOUT / SUCCESS` logged in `activity_log`

---

### 5.5 Password Reset

**Actor:** Any user (unauthenticated)

**Step 1 — Request reset:** `POST /api/auth/forgot-password`
1. Backend looks up email silently (no error if not found — security)
2. Generates UUID token → stores in `password_reset_tokens` (expiry: now + 30 min)
3. Sends email with reset link → Logs `PASSWORD_RESET_REQUEST / SUCCESS` → always returns `200`

**Step 2 — Submit new password:** `POST /api/auth/reset-password`
1. Backend validates token: exists + not expired + not used
2. BCrypt-hashes new password → updates `users.password`
3. Marks token `is_used=TRUE` → resets `failed_login_attempts=0`, `is_locked=FALSE`
4. Logs `PASSWORD_RESET / SUCCESS` → returns `200`
5. Frontend redirects to `/login`

**Error Flows:**

| Scenario | HTTP | Message |
|---|---|---|
| Email not registered | 200 | Generic (security by design) |
| Token expired | 400 | "Reset link has expired" |
| Token already used | 400 | "Reset link already used" |
| Weak new password | 400 | Password policy errors |

**Acceptance Criteria:**
- [ ] Token is single-use (UUID, 30-min TTL), marked used after success
- [ ] Locked accounts auto-unlocked on successful reset
- [ ] Both `PASSWORD_RESET_REQUEST` and `PASSWORD_RESET` logged

---

### 5.6 JWT Token Management

| Property | Access Token | Refresh Token |
|---|---|---|
| Algorithm | HS256 | HS256 |
| Validity | 24 hours | 7 days |
| Claims | userId, email, role, iat, exp | userId, iat, exp |
| Storage | Memory / HttpOnly cookie | HttpOnly cookie |

**Sample JWT payload:**
```json
{ "sub": "john@example.com", "userId": 101, "role": "CUSTOMER", "iat": 1741651200, "exp": 1741737600 }
```

**Acceptance Criteria:**
- [ ] Secret key ≥ 256-bit, configurable via environment variable
- [ ] Expired / tampered tokens → `401`
- [ ] Refresh endpoint issues new access token without re-login
- [ ] Blacklisted tokens rejected even before expiry

---

### 5.7 Role-Based Access Control

| Endpoint / Area | CUSTOMER | ADMIN |
|---|:---:|:---:|
| `/api/auth/**` | Public | Public |
| `/api/accounts/**`, `/api/transactions/**` | Yes | No |
| `/api/admin/**` | No | Yes |
| `/dashboard` | Yes | No |

- Unauthorized role → `403 Forbidden`
- No token → `401 Unauthorized`
- Role validated from JWT claims on every request via Spring Security filter

---

## 6. Business Rules

### Password Policy

Min 8 / Max 64 chars | At least 1 uppercase, 1 lowercase, 1 digit, 1 special char (`!@#$%^&*`) | BCrypt-hashed only | No last-password reuse on change

### Account Lockout

Lock after **5 consecutive failed logins** | Indefinite until admin unlocks OR password reset | Counter resets to 0 on successful login

### Token Expiry

| Token | Expiry |
|---|---|
| JWT Access | 24 hours |
| JWT Refresh | 7 days |
| Email Verification | 15 minutes |
| Mobile OTP | 15 minutes |
| Password Reset | 30 minutes |

### Compliance

- Activity logs retained **minimum 90 days** | Never deleted with user account
- Admin accounts pre-seeded only — no public registration endpoint
- Default admin must change password on first login
- Passwords and full tokens must never appear in application logs

---

## 7. API Contracts

### Endpoints

| Method | Path | Auth | Description |
|---|---|:---:|---|
| POST | `/api/auth/register` | No | Register new customer |
| GET | `/api/auth/verify-email?token=` | No | Verify email via link |
| POST | `/api/auth/verify-mobile` | No | Verify mobile via OTP |
| POST | `/api/auth/resend-verification` | No | Resend email link or mobile OTP |
| POST | `/api/auth/login` | No | Login → JWT |
| POST | `/api/auth/logout` | JWT | Invalidate token |
| POST | `/api/auth/forgot-password` | No | Request reset email |
| POST | `/api/auth/reset-password` | No | Submit new password |
| POST | `/api/auth/refresh-token` | Refresh | Issue new access token |

### Request / Response Payloads

**Register** `POST /api/auth/register`
```json
// Request
{ "fullName": "John Doe", "email": "john@example.com", "phoneNumber": "9876543210",
  "dateOfBirth": "1995-06-15", "address": "Mumbai", "password": "SecurePass@123" }
// 201 — account created, verification emails/SMS sent
{ "message": "Registration successful. Please verify your email and mobile number." }
```

**Verify Email** `GET /api/auth/verify-email?token=TOKEN`
```
// 200 — email verified
// 400 — token expired or already used
```

**Verify Mobile** `POST /api/auth/verify-mobile`
```json
// Request
{ "phoneNumber": "9876543210", "otp": "482910" }
// 200 — mobile verified, account activated if email also verified
// 400 — wrong OTP, expired, or already used
```

**Resend Verification** `POST /api/auth/resend-verification`
```json
// Request
{ "type": "EMAIL", "identifier": "john@example.com" }
{ "type": "MOBILE", "identifier": "9876543210" }
// 200 — resent | 429 — rate limit exceeded
```

**Login** `POST /api/auth/login`
```json
// Request
{ "email": "john@example.com", "password": "SecurePass@123" }

// 200 Response
{ "accessToken": "eyJ...", "refreshToken": "dGh...", "tokenType": "Bearer",
  "expiresIn": 86400, "role": "CUSTOMER", "userId": 101, "fullName": "John Doe" }
```

**Forgot Password** `POST /api/auth/forgot-password`
```json
// Request
{ "email": "john@example.com" }
// 200 — always
{ "message": "If this email is registered, a reset link has been sent." }
```

**Reset Password** `POST /api/auth/reset-password`
```json
// Request
{ "token": "550e8400-e29b-41d4-a716-446655440000", "newPassword": "NewSecure@456" }
// 200 on success | 400 on expired/used token or weak password
```

### HTTP Status Reference

| Code | Meaning |
|---|---|
| 200 / 201 | Success / Created |
| 400 | Validation / bad input |
| 401 | Invalid credentials / expired token |
| 403 | Access denied (wrong role, inactive, or unverified account) |
| 409 | Duplicate email or phone |
| 423 | Account locked |
| 429 | Rate limit exceeded (resend verification) |
| 500 | Server error (generic message to client) |

**Standard error body:**
```json
{ "timestamp": "2026-03-11T10:30:00", "status": 400, "error": "Bad Request",
  "message": "Validation failed", "path": "/api/auth/register",
  "errors": { "email": "must be a valid email address" } }
```

---

## 8. Frontend Pages (React)

| Page | Route | API Called | Key Fields |
|---|---|---|---|
| Login | `/login` | `POST /api/auth/login` | email, password, Remember Me |
| Register | `/register` | `POST /api/auth/register` | fullName, email, phone, DOB, address, password, confirmPassword |
| Verify Email | `/verify-email?token=` | `GET /api/auth/verify-email` | token (from URL) |
| Verify Mobile | `/verify-mobile` | `POST /api/auth/verify-mobile` | phoneNumber, otp (6-digit) |
| Forgot Password | `/forgot-password` | `POST /api/auth/forgot-password` | email |
| Reset Password | `/reset-password?token=` | `POST /api/auth/reset-password` | newPassword, confirmPassword |

**Common UI rules:**
- All forms validate client-side before API call
- Server `400` / `409` errors shown inline per field
- `401` / `423` / `403` shown as banner on Login page
- Password fields show a strength meter on Register and Reset pages
- After registration → show pending verification message (do not redirect to login yet)
- After both verifications pass → redirect to `/login` with "Account activated!" toast
- Verify Mobile page includes a "Resend OTP" link (disabled for 60s after each send)

---

## 9. Backend Components (Spring Boot)

### `JwtService.java`

| Method | Purpose |
|---|---|
| `generateAccessToken(user)` | Signs JWT with userId, email, role claims |
| `generateRefreshToken(user)` | Signs long-lived refresh token |
| `extractEmail / extractRole / extractUserId` | Parse claims from token |
| `isTokenValid(token, user)` | Checks expiry + user match |
| `blacklistToken / isTokenBlacklisted` | Redis / in-memory blacklist |

### `SecurityConfig.java`

| Item | Value |
|---|---|
| Password encoder | BCryptPasswordEncoder (strength 12) |
| Session | STATELESS |
| CSRF | Disabled |
| Public paths | `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` |
| JWT filter | `JwtAuthFilter` before UsernamePasswordAuthenticationFilter |
| CORS | Configured for React frontend origin |

### Entity → Repository → Service

| Entity | Key Fields | Repository Methods |
|---|---|---|
| `User` | email, password, isActive, isLocked, emailVerified, phoneVerified, failedLoginAttempts | `findByEmail`, `existsByEmail` |
| `Profile` | userId (FK), fullName, phoneNumber, dateOfBirth | `findByUserId`, `existsByPhoneNumber` |
| `Role` | roleName | `findByRoleName` |
| `VerificationToken` | userId, token, type (EMAIL/MOBILE), expiryDate, isUsed | `findByTokenAndType`, `findByUserIdAndType` |
| `PasswordResetToken` | token, expiryDate, isUsed | `findByToken`, `deleteAllByUserId` |
| `ActivityLog` | userId, activityType, status | `findByUserIdOrderByCreatedAtDesc` |

### `AuthService.java` Methods

| Method | Action |
|---|---|
| `register(dto)` | Creates User + Profile + assigns role + sends email & OTP (transactional) |
| `verifyEmail(token)` | Validates EMAIL token, sets emailVerified=TRUE, activates if phone also verified |
| `verifyMobile(phone, otp)` | Validates MOBILE token, sets phoneVerified=TRUE, activates if email also verified |
| `resendVerification(type, identifier)` | Invalidates old token, generates new one, re-sends (rate-limited) |
| `login(dto)` | Validates creds + is_active, manages lockout counter, returns JWT |
| `logout(token)` | Blacklists token, logs event |
| `forgotPassword(email)` | Creates reset token, sends email |
| `resetPassword(token, pwd)` | Validates token, updates password, clears lockout |
| `refreshToken(token)` | Validates refresh token, issues new access token |
| `logActivity(userId, type, status, ip, ua)` | Writes to `activity_log` |

### DTOs

| DTO | Fields |
|---|---|
| `RegisterRequest` | fullName, email, phoneNumber, dateOfBirth, address, password |
| `VerifyMobileRequest` | phoneNumber, otp |
| `ResendVerificationRequest` | type (EMAIL/MOBILE), identifier |
| `LoginRequest` | email, password |
| `ForgotPasswordRequest` | email |
| `ResetPasswordRequest` | token, newPassword |
| `AuthResponse` | accessToken, refreshToken, tokenType, expiresIn, role, userId, fullName |

All request DTOs use Bean Validation (`@NotBlank`, `@Email`, `@Past`, `@ValidPassword`, etc.).

---

## 10. Security Checklist

| Item | Approach |
|---|---|
| Password storage | BCrypt (strength 12) — never plain text |
| JWT integrity | HS256, secret ≥ 256-bit, environment variable |
| Sessions | Stateless (no server-side sessions) |
| Transport | HTTPS enforced (TLS at deployment level) |
| SQL injection | Spring Data JPA parameterized queries |
| Input validation | Bean Validation on all DTOs |
| CORS | Restricted to configured frontend origin |
| Token revocation | Blacklist on logout (Redis / in-memory) |
| Sensitive logging | Passwords never logged; tokens partial-hash only |
| PII exposure | Email never returned in error messages |

---

*End of Authentication Module BRD — v1.0*

