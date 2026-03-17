# Module 05 Balance Enquiry Service - API Design

## Module Summary
Read-only API contracts for account balance snapshot and mini statement preview.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Protected APIs require `Authorization: Bearer <jwt>`
- Standard response envelope matches Modules 01-04 (`success`, `timestamp`, `data/error`)

## Endpoint Catalog

### Net-New Module 05 Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/balances/accounts/{accountId}` | Customer balance snapshot for owned account | Customer |
| GET | `/api/balances/accounts/{accountId}/mini-statement` | Customer mini statement preview | Customer |
| GET | `/api/admin/balances/accounts/{accountId}` | Admin balance snapshot for any account | Admin |
| GET | `/api/admin/balances/accounts/{accountId}/mini-statement` | Admin mini statement preview | Admin |

### Reused Existing Endpoints (No Duplication)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/accounts` | Account listing for dropdown/context | Reused |
| GET | `/api/accounts/{accountId}` | Account metadata context | Reused |

## Request/Response Contracts

### 1) Balance Snapshot (Customer)
`GET /api/balances/accounts/{accountId}`

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-18T14:20:00Z",
  "data": {
	"accountId": 5001,
	"accountNumber": "123456789012",
	"currencyCode": "INR",
	"status": "ACTIVE",
	"availableBalance": 3450.0,
	"ledgerBalance": 3450.0,
	"asOf": "2026-03-18T14:19:58Z"
  }
}
```

### 2) Mini Statement Preview
`GET /api/balances/accounts/{accountId}/mini-statement?limit=10`

Response includes latest transaction items sorted by `createdAt desc`.

## Status Codes
- `200 OK`: enquiry succeeded
- `400 Bad Request`: invalid account id/filter/limit
- `401 Unauthorized` / `403 Forbidden`: authn/authz failure
- `404 Not Found`: account not found
- `500 Internal Server Error`: unexpected server error

## Error Codes (Suggested Module 05)
- `BALANCE_ACCOUNT_NOT_FOUND`
- `BALANCE_ACCOUNT_FORBIDDEN`
- `BALANCE_INVALID_QUERY`

## API Notes
- Mini statement `limit` should be bounded (for example max 50).
- This is read-only; idempotency key is not required.
- Non-duplication: reads from Module 04 persisted balances/ledger only.
