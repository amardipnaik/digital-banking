# Module 01 Authentication - API Design

## Module Summary
API contract for role-based authentication, verification, password reset, login activity tracking, and admin account control.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Auth header for protected APIs: `Authorization: Bearer <jwt>`
- Login identifier supports either email or mobile number.
- All responses use a consistent envelope.

### Success Response Envelope
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {}
}
```

### Error Response Envelope
```json
{
  "success": false,
  "timestamp": "2026-03-11T17:30:00Z",
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "Invalid login credentials.",
    "details": []
  }
}
```

## Endpoint List

### Public Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register/customer` | Register customer account | Public |
| POST | `/api/auth/login` | Login with email/mobile + password | Public |
| POST | `/api/auth/verification/request` | Issue verification token/OTP for email or mobile | Public |
| POST | `/api/auth/verification/confirm` | Confirm verification token/OTP for email or mobile | Public |
| POST | `/api/auth/password/forgot` | Create password reset token | Public |
| POST | `/api/auth/password/reset` | Reset password with token | Public |

### Authenticated Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/logout` | Logout current session/token | Authenticated |
| GET | `/api/auth/me` | Current authenticated user summary | Authenticated |

### Admin Endpoints (Account Controls)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| PATCH | `/api/admin/auth/users/{userId}/status` | Update user status (`ACTIVE`/`DISABLED`) | Admin |

## Request and Response Contracts

### 1) Register Customer
`POST /api/auth/register/customer`

Request:
```json
{
  "fullName": "Anita Sharma",
  "email": "anita@example.com",
  "mobileNumber": "9876543210",
  "dateOfBirth": "1994-07-21",
  "password": "Password1",
  "confirmPassword": "Password1"
}
```

Response (`201 Created`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "userId": 2004,
    "role": "CUSTOMER",
    "status": "PENDING_VERIFICATION",
    "emailVerified": false,
    "mobileVerified": false
  }
}
```

### 2) Login
`POST /api/auth/login`

Request:
```json
{
  "loginId": "anita@example.com",
  "password": "Password1",
  "deviceId": "DEVICE-123"
}
```

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 2001,
      "role": "CUSTOMER",
      "status": "ACTIVE"
    }
  }
}
```

Business behavior:
- On each failed password attempt, increment `failed_login_attempts` and write `login_activity_logs`.
- At 3 failed attempts, set status to `LOCKED`.
- `DISABLED` users cannot login.
- Customer login requires both email and mobile verification.
- `User-Agent` is captured from request header (not request body).

### 3) Verification Request
`POST /api/auth/verification/request`

Request:
```json
{
  "loginId": "anita@example.com",
  "channel": "EMAIL"
}
```

Response (`200 OK`): token generated in `auth_tokens` based on channel.
- `channel=EMAIL` -> `token_type=EMAIL_VERIFY`
- `channel=MOBILE` -> `token_type=MOBILE_VERIFY`

### 4) Verification Confirm
`POST /api/auth/verification/confirm`

Request:
```json
{
  "loginId": "anita@example.com",
  "channel": "EMAIL",
  "token": "123456"
}
```

Response (`200 OK`): marks target channel verified and consumes token (`is_used=true`, `consumed_at` set).

### 5) Forgot Password
`POST /api/auth/password/forgot`

Request:
```json
{
  "loginId": "anita@example.com"
}
```

Response (`200 OK`): creates `auth_tokens` row with `token_type=PASSWORD_RESET` and `channel=null`.

### 6) Reset Password
`POST /api/auth/password/reset`

Request:
```json
{
  "loginId": "anita@example.com",
  "token": "reset-token-value",
  "newPassword": "Password1",
  "confirmPassword": "Password1"
}
```

Response (`200 OK`): password changed, token consumed, failed login count reset.

### 7) Logout
`POST /api/auth/logout`

Request:
```json
{}
```

Response (`200 OK`): logout accepted.

Logout strategy (explicit v1 behavior):
- Access token is short-lived JWT.
- Client must discard token on logout.
- If revocation is enabled, API stores token `jti` in denylist cache until token expiry.

### 8) Authenticated User Summary
`GET /api/auth/me`

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "id": 2001,
    "role": "CUSTOMER",
    "email": "anita@example.com",
    "mobileNumber": "9876543210",
    "status": "ACTIVE",
    "emailVerified": true,
    "mobileVerified": true
  }
}
```

### 9) Update User Status (Admin)
`PATCH /api/admin/auth/users/{userId}/status`

Request:
```json
{
  "status": "DISABLED",
  "reason": "Suspicious login attempts"
}
```

Response (`200 OK`):
- `status=DISABLED`: sets `users.status='DISABLED'`, stores reason/by/at.
- `status=ACTIVE`: sets `users.status='ACTIVE'`, clears disable and lock fields.

## Status Codes
- `200 OK`: successful action.
- `201 Created`: successful customer registration.
- `400 Bad Request`: validation error, invalid request state.
- `401 Unauthorized`: missing/invalid JWT or credentials.
- `403 Forbidden`: role/access denied.
- `404 Not Found`: user/token not found.
- `409 Conflict`: duplicate email/mobile, active-token uniqueness conflict.
- `423 Locked`: account locked due to max failed attempts.
- `429 Too Many Requests`: request throttled (if rate limiter is enabled).
- `500 Internal Server Error`: unexpected server error.

## Standard Error Codes
- `AUTH_INVALID_CREDENTIALS`
- `AUTH_ACCOUNT_LOCKED`
- `AUTH_ACCOUNT_DISABLED`
- `AUTH_EMAIL_NOT_VERIFIED`
- `AUTH_MOBILE_NOT_VERIFIED`
- `AUTH_MAX_LOGIN_ATTEMPTS_REACHED`
- `AUTH_TOKEN_INVALID`
- `AUTH_TOKEN_EXPIRED`
- `AUTH_TOKEN_ALREADY_USED`
- `AUTH_MAX_TOKEN_ATTEMPTS_REACHED`
- `AUTH_USER_ALREADY_EXISTS`
- `AUTH_ACCESS_DENIED`
- `AUTH_INVALID_STATUS_TRANSITION`

## DB Mapping Notes
- `users.status` drives account state checks (`PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED`).
- `auth_tokens` handles email verify, mobile verify, and password reset via `token_type`.
- `login_activity_logs` captures each login attempt with success/failure/blocked/disabled result.
- Unique active token rule is enforced by `user_id + token_type + channel`.
