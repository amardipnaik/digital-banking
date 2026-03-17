# Module 04 Transaction Processing - API Design

## Module Summary
API contracts for deposit, withdrawal, transfer, and operational transaction controls.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Protected APIs require `Authorization: Bearer <jwt>`
- Standard response envelope matches Modules 01-03 (`success`, `timestamp`, `data/error`)
- Client sends `Idempotency-Key` header (or request field equivalent) for create/post operations

## Endpoint Catalog

### Net-New Module 04 Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/transactions/deposit` | Post deposit to owned account | Customer |
| POST | `/api/transactions/withdrawal` | Post withdrawal from owned account | Customer |
| POST | `/api/transactions/transfer` | Post transfer between eligible accounts | Customer |
| POST | `/api/admin/transactions/adjustment` | Post admin adjustment | Admin |
| POST | `/api/admin/transactions/reversal` | Post reversal for existing transaction | Admin |

### Reused Existing Endpoints (No Duplication)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/accounts` | Account context for transaction source selection | Reused |
| GET | `/api/admin/accounts` | Admin account lookup/filter context | Reused |

## Request/Response Contracts

### 1) Deposit
`POST /api/transactions/deposit`

Request example:
```json
{
  "accountId": 5001,
  "amount": 1000.00,
  "currencyCode": "INR",
  "remarks": "Cash deposit"
}
```

Response (`200 OK`):
```json
{
  "success": true,
  "timestamp": "2026-03-18T11:30:00Z",
  "data": {
	"transactionRef": "TXN-20260318-000001",
	"accountId": 5001,
	"type": "DEPOSIT",
	"status": "POSTED",
	"amount": 1000.0,
	"balanceAfter": 4500.0
  }
}
```

### 2) Withdrawal
`POST /api/transactions/withdrawal`

Request fields mirror deposit. Behavior includes insufficient-funds check.

### 3) Transfer
`POST /api/transactions/transfer`

Request example:
```json
{
  "sourceAccountId": 5001,
  "targetAccountId": 5002,
  "amount": 250.0,
  "currencyCode": "INR",
  "remarks": "Own account transfer"
}
```

Response includes transfer group reference plus debit and credit transaction refs.

### 4) Admin Adjustment
`POST /api/admin/transactions/adjustment`

Request includes `accountId`, `entrySide`, `amount`, and mandatory `reason`.

### 5) Admin Reversal
`POST /api/admin/transactions/reversal`

Request includes `originalTransactionRef` and mandatory `reason`.

## Status Codes
- `200 OK`: posting succeeded
- `400 Bad Request`: validation/eligibility/funds/idempotency issue
- `401 Unauthorized` / `403 Forbidden`: authn/authz failure
- `404 Not Found`: account or transaction reference not found
- `409 Conflict`: duplicate idempotency / posting conflict
- `500 Internal Server Error`: unexpected failure

## Error Codes (Suggested Module 04)
- `TRANSACTION_ACCOUNT_NOT_FOUND`
- `TRANSACTION_ACCOUNT_NOT_ELIGIBLE`
- `TRANSACTION_INSUFFICIENT_FUNDS`
- `TRANSACTION_DUPLICATE_REQUEST`
- `TRANSACTION_INVALID_TRANSFER`
- `TRANSACTION_ALREADY_REVERSED`

## API Notes
- Idempotency: same idempotency key + same payload should return deterministic prior result.
- Atomicity: transfer writes both entries or none.
- Non-duplication: no duplicate account/customer/auth APIs; reuse Modules 01-03 contracts.
