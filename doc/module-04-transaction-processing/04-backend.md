# Module 04 Transaction Processing - Backend Design

## Module Summary
Transactional backend for financial postings with atomic balance updates and immutable ledger persistence.

## Controller Boundaries
- `TransactionController` under `/api/transactions` for customer postings.
- `AdminTransactionController` under `/api/admin/transactions` for operational adjustments/reversals.
- Keep account lifecycle APIs (`/api/accounts`, `/api/admin/accounts`) unchanged.

## Service Responsibilities
- Validate account eligibility and ownership before posting.
- Enforce idempotency key checks for duplicate submission protection.
- Execute deposit/withdraw/transfer in ACID transaction boundary.
- Update `accounts.available_balance` and `accounts.ledger_balance` consistently.
- Persist immutable ledger rows into `account_transactions`.
- Create transfer-linked rows with shared `transfer_group_ref`.
- Support admin adjustment/reversal flow with strict reason/audit controls.

## Repository and Mapping Strategy
- Add entity/repository for `account_transactions`.
- Reuse `Account` and `User` mappings from Module 03 and earlier modules.
- Use pessimistic/optimistic locking strategy for concurrent postings on same account.
- Avoid duplicate mappings for existing tables.

## DTOs / Models
- Request DTOs:
  - customer transaction post request
  - transfer request (source + target)
  - admin adjustment/reversal request
- Response DTOs:
  - transaction post response (refs + status)
  - transaction detail summary
  - paginated transaction list item (for admin/ops)

## Validation and Error Handling
- Validate amount, currency, account status, and transfer account distinctness.
- Validate sufficient funds for debit postings.
- Validate idempotency uniqueness and replay behavior.
- Return errors via existing `ApiResponse` envelope.
- Suggested error codes: `TRANSACTION_ACCOUNT_NOT_FOUND`, `TRANSACTION_ACCOUNT_NOT_ELIGIBLE`, `TRANSACTION_INSUFFICIENT_FUNDS`, `TRANSACTION_DUPLICATE_REQUEST`, `TRANSACTION_INVALID_TRANSFER`.

## Security / Authorization
- `/api/transactions/**`: authenticated customer context and owned-account restriction.
- `/api/admin/transactions/**`: `ROLE_ADMIN` only.
- Preserve existing JWT filters and role guard setup from Module 01.
- Ensure blocked statuses (`FROZEN`, `CLOSED`) cannot be posted by customer endpoints.
