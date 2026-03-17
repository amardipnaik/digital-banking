# Module 05 Balance Enquiry Service - Backend Design

## Module Summary
Read-only balance enquiry backend over Module 04 account and ledger data.

## Controller Boundaries
- `BalanceController` under `/api/balances` for customer-owned account enquiries.
- `AdminBalanceController` under `/api/admin/balances` for operational/admin enquiries.
- Keep transaction posting controllers unchanged.

## Service Responsibilities
- Resolve account ownership and access scope.
- Fetch account balance snapshot from `accounts`.
- Fetch mini statement preview from `account_transactions` (latest N entries).
- Return standardized response DTOs for summary and mini statement rows.
- Keep module read-only and side-effect free.

## Repository and Mapping Strategy
- Reuse `Account` mapping from Module 03/04.
- Reuse `AccountTransaction` mapping from Module 04.
- Add projection/query methods for recent transactions and lightweight admin lookup.
- Avoid duplicate entity mappings for existing tables.

## DTOs / Models
- Request DTOs:
  - account balance query request (if body-based filters are used)
  - admin balance filter request/query params
- Response DTOs:
  - balance summary response
  - mini statement item response
  - combined enquiry response with pagination metadata for preview rows

## Validation and Error Handling
- Validate account existence and ownership for customer endpoints.
- Validate admin filters and optional limits for preview row count.
- Return errors via standard `ApiResponse` envelope.
- Suggested error codes: `BALANCE_ACCOUNT_NOT_FOUND`, `BALANCE_ACCOUNT_FORBIDDEN`, `BALANCE_INVALID_QUERY`.

## Security / Authorization
- `/api/balances/**`: authenticated customer, owned account only.
- `/api/admin/balances/**`: `ROLE_ADMIN` only.
- Preserve existing JWT filter and role guard behavior.
- Ensure soft-deleted/non-eligible users cannot access customer enquiry endpoints.
