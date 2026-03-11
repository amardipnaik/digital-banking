# Module 01 Authentication - API Design

## Module Summary
API contract for registration, login, verification, password reset, current-user details, and admin status controls.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Protected APIs require `Authorization: Bearer <jwt>`
- `loginId` accepts either email or mobile number
- Standard response envelope for all APIs

## Response Envelope

Success:
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {}
}
```

Error:
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

## Endpoint Catalog

### Public
- `POST /api/auth/register/customer`
- `POST /api/auth/login`
- `POST /api/auth/verification/request`
- `POST /api/auth/verification/confirm`
- `POST /api/auth/password/forgot`
- `POST /api/auth/password/reset`

### Authenticated
- `POST /api/auth/logout`
- `GET /api/auth/me`

### Admin
- `PATCH /api/admin/auth/users/{userId}/status`

## Contracts

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
    "id": 2004,
    "role": "CUSTOMER",
    "status": "PENDING_VERIFICATION",
    "email": "anita@example.com",
    "mobileNumber": "9876543210",
    "emailVerified": false,
    "mobileVerified": false
  }
}
```

### 2) Login
`POST /api/auth/login`

Request (`deviceId` optional):
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
      "id": 2004,
      "role": "CUSTOMER",
      "status": "PENDING_VERIFICATION",
      "email": "anita@example.com",
      "mobileNumber": "9876543210",
      "emailVerified": false,
      "mobileVerified": false
    }
  }
}
```

Login behavior:
- Wrong password increments failed attempts and logs activity.
- At 3 failed attempts account is locked (`LOCKED`).
- `DISABLED` users are blocked.
- `PENDING_VERIFICATION` users are allowed to login.
- `User-Agent` is captured from request header.

### 3) Request Verification OTP
`POST /api/auth/verification/request`

Request:
```json
{
  "loginId": "anita@example.com",
  "channel": "EMAIL"
}
```

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "message": "Verification token generated successfully"
  }
}
```

### 4) Confirm Verification OTP
`POST /api/auth/verification/confirm`

Request:
```json
{
  "loginId": "anita@example.com",
  "channel": "EMAIL",
  "token": "123456"
}
```

Response (`200 OK`): target channel is marked verified; token is consumed.

### 5) Forgot Password
`POST /api/auth/password/forgot`

Request:
```json
{
  "loginId": "anita@example.com"
}
```

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "message": "If account exists, password reset instructions have been generated"
  }
}
```

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

Response (`200 OK`): password hash updated and reset token consumed.

### 7) Logout
`POST /api/auth/logout`

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "message": "Logout successful"
  }
}
```

### 8) Current User
`GET /api/auth/me`

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-11T17:30:00Z",
  "data": {
    "id": 2004,
    "role": "CUSTOMER",
    "email": "anita@example.com",
    "mobileNumber": "9876543210",
    "status": "PENDING_VERIFICATION",
    "emailVerified": false,
    "mobileVerified": false
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

Rules:
- Only `ACTIVE` and `DISABLED` accepted.
- `DISABLED` stores reason/by/at metadata.
- `ACTIVE` clears disable and lock fields.

## HTTP Status Codes
- `200 OK`
- `201 Created`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `423 Locked`
- `500 Internal Server Error`

## Standard Error Codes (Current Implementation)
- `AUTH_ACCOUNT_DISABLED`
- `AUTH_ACCOUNT_LOCKED`
- `AUTH_ACCOUNT_NOT_ACTIVE`
- `AUTH_INVALID_CREDENTIALS`
- `AUTH_INVALID_STATUS_TRANSITION`
- `AUTH_MAX_LOGIN_ATTEMPTS_REACHED`
- `AUTH_MAX_TOKEN_ATTEMPTS_REACHED`
- `AUTH_PASSWORD_MISMATCH`
- `AUTH_TOKEN_ALREADY_USED`
- `AUTH_TOKEN_EXPIRED`
- `AUTH_TOKEN_INVALID`
- `AUTH_UNAUTHORIZED`
- `AUTH_USER_ALREADY_EXISTS`
- `AUTH_USER_NOT_FOUND`

## DB Mapping Notes
- `users.status` values: `PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED`.
- `auth_tokens` stores all verification and reset token records.
- `login_activity_logs` stores each login attempt and metadata.
- Unique active token rule: `user_id + token_type + channel`.
