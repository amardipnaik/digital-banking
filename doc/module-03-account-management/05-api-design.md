# Module 03 Account Management - API Design

## Module Summary
Account lifecycle APIs for customer self-service and admin controls using existing auth/customer foundations.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Protected APIs require `Authorization: Bearer <jwt>`
- Standard envelope matches Modules 01/02 (`success`, `timestamp`, `data/error`)

## Endpoint Catalog

### Net-New Module 03 Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/accounts` | Create account request for logged-in customer | Customer |
| GET | `/api/accounts` | List logged-in customer accounts | Customer |
| GET | `/api/accounts/{accountId}` | Get logged-in customer account detail | Customer |
| GET | `/api/admin/accounts` | List accounts with admin filters | Admin |
| GET | `/api/admin/accounts/{accountId}` | Get account detail for admin | Admin |
| PATCH | `/api/admin/accounts/{accountId}/status` | Update lifecycle status | Admin |
| GET | `/api/admin/accounts/{accountId}/history` | Get account status timeline | Admin |

### Reused Existing Endpoints (No Duplication)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/auth/me` | Authenticated user context for UI/session | Reused |
| GET | `/api/admin/customers/{userId}` | Customer detail/KYC context when needed in admin views | Reused |

## Query Parameters
- Customer list API: `page`, `size`, `sort`
- Admin list API: `search`, `status`, `accountType`, `userId`, `page`, `size`, `sort`

## Contract Sketches

### 1) Create Account
`POST /api/accounts`

Request example:
```json
{
  "accountType": "SAVINGS",
  "currencyCode": "INR"
}
```

Response (`201 Created`):
```json
{
  "success": true,
  "timestamp": "2026-03-17T10:00:00Z",
  "data": {
	"accountId": 5001,
	"accountNumber": "123456789012",
	"accountType": "SAVINGS",
	"currencyCode": "INR",
	"status": "PENDING_APPROVAL"
  }
}
```

### 2) Customer Account List
`GET /api/accounts?page=0&size=10&sort=createdAt,desc`

Response: paginated customer-own accounts.

### 3) Admin Account List
`GET /api/admin/accounts?search=anita&status=ACTIVE&accountType=SAVINGS&page=0&size=20`

Response: paginated account view with customer identity summary.

### 4) Admin Status Update
`PATCH /api/admin/accounts/{accountId}/status`

Request example:
```json
{
  "status": "FROZEN",
  "reason": "Compliance hold"
}
```

Behavior: validates transition matrix and appends timeline entry.

### 5) Account History
`GET /api/admin/accounts/{accountId}/history?page=0&size=20`

Response: status transition timeline with actor and reason.

## Status Codes
- `200 OK`: read/update succeeded
- `201 Created`: account created
- `400 Bad Request`: invalid request or invalid transition
- `401 Unauthorized` / `403 Forbidden`: authn/authz failure
- `404 Not Found`: account not found
- `409 Conflict`: account number conflict (rare, on uniqueness violation)
- `500 Internal Server Error`: unexpected server error

## Error Codes (Suggested Module 03)
- `ACCOUNT_NOT_FOUND`
- `ACCOUNT_NOT_ELIGIBLE`
- `ACCOUNT_INVALID_TRANSITION`
- `ACCOUNT_NUMBER_CONFLICT`
- `ACCOUNT_FORBIDDEN`

## Non-Duplication Notes
- Keep account status separate from `users.status`; do not overload auth status for account lifecycle.
- Do not introduce duplicate customer profile APIs.
- Reuse existing auth/session and response envelope contracts unchanged.
