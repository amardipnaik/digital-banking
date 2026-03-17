# Module 06 Transaction History - Requirements

## Module Summary
Detailed, paginated, and filterable transaction history for customer self-service and admin investigation.

## Scope
- In scope: customer transaction history for owned accounts with pagination and filters.
- In scope: admin transaction history lookup across accounts/customers.
- In scope: transaction detail view by reference and transfer-group linking.
- Out of scope: transaction posting/correction logic (Module 04).
- Out of scope: balance snapshot API ownership (Module 05).

## Guardrails (Non-Duplication)
- Reuse `account_transactions` as canonical history source.
- Reuse `accounts` and `users` mapping for ownership and admin lookup context.
- Do not create duplicate history storage/table per channel.
- Keep response envelope/error conventions aligned with Modules 01-05.

## Functional Requirements
- FR-1: Customer can list history for owned account with pagination.
- FR-2: Customer can filter by date range, transaction type, entry side, and status.
- FR-3: Customer can retrieve a single transaction detail by transaction reference if ownership is valid.
- FR-4: Admin can query history across customers/accounts for investigation.
- FR-5: Transfer entries should expose link/reference to counterpart entry via `transferGroupRef`.
- FR-6: Reversed transactions should be clearly marked and traceable to reversal entry.

## Non-Functional Requirements
- Performance: history list APIs must be index-backed and stable under pagination.
- Security: customer endpoints enforce ownership; admin endpoints require `ROLE_ADMIN`.
- Validation: date-range and pagination limits must be validated.
- Auditability: preserve immutable ordering and transaction references from ledger.

## User Roles
- `CUSTOMER`: view own transaction history and details.
- `ADMIN`: view cross-account transaction history and details.

## Acceptance Criteria
- AC-1: Module 06 reads from Module 04 ledger without data duplication.
- AC-2: Customer cannot access history of non-owned accounts.
- AC-3: Filters and pagination return deterministic ordered results.
- AC-4: Transaction detail API returns transfer/reversal linkage metadata when applicable.
- AC-5: Existing modules remain backward compatible.
