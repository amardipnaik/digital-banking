# Module 06 Transaction History - Backend Design

## Module Summary
Read-only backend for historical transaction listing and transaction detail retrieval.

## Controller Boundaries
- `TransactionHistoryController` under `/api/transactions/history` for customer history APIs.
- `AdminTransactionHistoryController` under `/api/admin/transactions/history` for admin exploration APIs.
- Keep transaction posting controllers from Module 04 unchanged.

## Service Responsibilities
- Build pageable history query with validated filters.
- Enforce ownership checks for customer history endpoints.
- Resolve transaction detail by reference and include linked transfer/reversal metadata.
- Provide stable ordering and deterministic pagination.
- Keep module strictly read-only.

## Repository and Mapping Strategy
- Reuse `AccountTransaction` entity and repository extensions for filtered paging.
- Reuse `Account` ownership mapping and `User` role checks.
- Prefer projection DTO queries for list APIs to reduce payload overhead.
- Avoid duplicate entity mappings or duplicate history tables.

## DTOs / Models
- Request DTOs:
  - history filter request/query model
  - detail lookup request by `transactionRef`
- Response DTOs:
  - paginated history list response
  - history list item response
  - transaction detail response with linkage metadata

## Validation and Error Handling
- Validate account id ownership, date range, and pagination bounds.
- Validate transaction reference format before lookup.
- Return errors through existing `ApiResponse` envelope.
- Suggested error codes: `HISTORY_ACCOUNT_NOT_FOUND`, `HISTORY_ACCOUNT_FORBIDDEN`, `HISTORY_TRANSACTION_NOT_FOUND`, `HISTORY_INVALID_FILTER`.

## Security / Authorization
- `/api/transactions/history/**`: authenticated customer; owned accounts only.
- `/api/admin/transactions/history/**`: `ROLE_ADMIN` only.
- Reuse existing JWT/role guards from Modules 01-05.
- Ensure data leakage prevention in cross-account query paths.
