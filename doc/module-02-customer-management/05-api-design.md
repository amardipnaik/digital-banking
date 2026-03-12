# Module 02 Customer Management - API Design

## Module Summary
Admin APIs for customer listing, profile update, KYC management, and soft-delete built on existing auth schema and contracts.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Protected APIs require `Authorization: Bearer <jwt>`
- Standard envelope matches Module 01 (`success`, `timestamp`, `data/error`)

## Endpoint Catalog

### Net-New Module 02 Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/admin/customers` | List customers with filters and pagination | Admin |
| GET | `/api/admin/customers/{userId}` | Get customer detail by user id | Admin |
| PATCH | `/api/admin/customers/{userId}` | Update customer profile fields | Admin |
| PATCH | `/api/admin/customers/{userId}/kyc` | Update KYC status and remarks | Admin |
| DELETE | `/api/admin/customers/{userId}` | Soft-delete customer (`users.is_deleted = true`) | Admin |
| PATCH | `/api/admin/customers/{userId}/restore` | Restore soft-deleted customer | Admin |
| GET | `/api/admin/customers/{userId}/activity` | Get admin action timeline for customer | Admin |

### Reused Existing Endpoint (Auth Module)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| PATCH | `/api/admin/auth/users/{userId}/status` | ACTIVE/DISABLED account status updates | Admin |
| POST | `/api/auth/verification/request` | Verification resend orchestration using `loginId` + `channel` | Reused by Admin UI |

## Query Parameters for List API
- `search` (matches full name/email/mobile)
- `kycStatus` (`PENDING`, `APPROVED`, `REJECTED`)
- `userStatus` (`PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED`)
- `page`, `size`, `sort`

## Contract Sketches
### 1) List Customers
`GET /api/admin/customers?search=anita&kycStatus=APPROVED&userStatus=ACTIVE&page=0&size=10&sort=createdAt,desc`

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-13T10:00:00Z",
  "data": {
	"items": [
	  {
		"userId": 2001,
		"fullName": "Anita Sharma",
		"email": "anita.customer@digitalbank.test",
		"mobileNumber": "9000000002",
		"userStatus": "ACTIVE",
		"kycStatus": "APPROVED"
	  }
	],
	"page": 0,
	"size": 10,
	"totalElements": 1,
	"totalPages": 1
  }
}
```

### 2) Customer Detail
`GET /api/admin/customers/{userId}`

Response (`200 OK`): returns merged view of `users` + `customer_profiles`.

### 3) Update Profile
`PATCH /api/admin/customers/{userId}`

Request example:
```json
{
  "fullName": "Anita Sharma",
  "dateOfBirth": "1994-07-21",
  "addressLine1": "Flat 101, Green Residency",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postalCode": "400001",
  "country": "India",
  "governmentId": "ABCDE1234F",
  "governmentIdType": "PAN"
}
```

### 4) Update KYC
`PATCH /api/admin/customers/{userId}/kyc`

Request example:
```json
{
  "kycStatus": "APPROVED",
  "remarks": "Documents verified"
}
```

### 5) Soft Delete
`DELETE /api/admin/customers/{userId}`

Behavior: marks user as deleted; does not hard-delete records.

### 6) Restore Customer
`PATCH /api/admin/customers/{userId}/restore`

Behavior: clears delete markers and returns customer to default listing.

### 7) Customer Activity Timeline
`GET /api/admin/customers/{userId}/activity?page=0&size=20`

Response: paginated admin actions such as PROFILE_UPDATED, KYC_UPDATED, STATUS_UPDATED, SOFT_DELETED, RESTORED.

## Status Codes
- `200 OK`: read/update/delete succeeded
- `400 Bad Request`: validation or invalid transition
- `401 Unauthorized` / `403 Forbidden`: authn/authz failure
- `404 Not Found`: customer not found
- `409 Conflict`: unique value conflict (for example government ID)
- `500 Internal Server Error`: unexpected server error

## Error Codes (Suggested Module 02)
- `CUSTOMER_NOT_FOUND`
- `CUSTOMER_KYC_INVALID_TRANSITION`
- `CUSTOMER_GOVERNMENT_ID_CONFLICT`
- `CUSTOMER_ALREADY_DELETED`
- `CUSTOMER_NOT_DELETED`

## Non-Duplication Notes
- Customer creation remains in Module 01 via `POST /api/auth/register/customer`.
- Do not introduce `/api/admin/customers/{userId}/status`.
- Use existing auth status endpoint for ACTIVE/DISABLED.
- Use existing verification request endpoint for resend behavior instead of adding a second verification API.
- Keep all auth API contracts unchanged.
