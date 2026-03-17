# Module 06 Transaction History - API Design

## Module Summary
API contracts for transaction history listing and detail retrieval for customer and admin users.

## API Conventions
- Base path: `/api`
- Content type: `application/json`
- Protected APIs require `Authorization: Bearer <jwt>`
- Standard response envelope matches Modules 01-05 (`success`, `timestamp`, `data/error`)

## Endpoint Catalog

### Net-New Module 06 Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/transactions/history` | Customer transaction history list | Customer |
| GET | `/api/transactions/history/{transactionRef}` | Customer transaction detail | Customer |
| GET | `/api/admin/transactions/history` | Admin transaction history list | Admin |
| GET | `/api/admin/transactions/history/{transactionRef}` | Admin transaction detail | Admin |

### Reused Existing Endpoints (No Duplication)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/accounts` | Account context for filters | Reused |
| GET | `/api/balances/accounts/{accountId}` | Balance context while reviewing history | Reused |

## Request/Response Contracts

### 1) Customer History List
`GET /api/transactions/history?accountId=5001&type=TRANSFER&entrySide=DEBIT&status=POSTED&from=2026-03-01T00:00:00Z&to=2026-03-18T23:59:59Z&page=0&size=20&sort=createdAt,desc`

Response (`200 OK`) includes paginated items and metadata.

### 2) Transaction Detail
`GET /api/transactions/history/{transactionRef}`

Response includes transaction fields plus optional linkage:
- `transferGroupRef`
- `counterpartyAccountId`
- `reversalRef` (if reversed)

## Status Codes
- `200 OK`: query succeeded
- `400 Bad Request`: invalid filters/date range/pagination
- `401 Unauthorized` / `403 Forbidden`: authn/authz failure
- `404 Not Found`: account/transaction not found
- `500 Internal Server Error`: unexpected server error

## Error Codes (Suggested Module 06)
- `HISTORY_ACCOUNT_NOT_FOUND`
- `HISTORY_ACCOUNT_FORBIDDEN`
- `HISTORY_TRANSACTION_NOT_FOUND`
- `HISTORY_INVALID_FILTER`

## API Notes
- Pagination/filtering are mandatory for list endpoints at scale.
- Read-only module; idempotency key is not required.
- Non-duplication: list/detail read directly from Module 04 ledger (`account_transactions`).
