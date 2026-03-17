# Module 04 Transaction Processing - Requirements

## Module Summary
Customer and admin transaction workflows for deposit, withdrawal, transfer, and reversal using Module 03 account lifecycle as foundation.

## Scope
- In scope: post financial transactions on eligible accounts (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`).
- In scope: transactional validation (account status, ownership, funds check, idempotency key handling).
- In scope: immutable transaction ledger entries for audit and later reporting.
- In scope: admin-initiated operational adjustments/reversals with mandatory reason.
- Out of scope: statement UI/reporting and pagination-heavy history exploration (Module 06).
- Out of scope: rebuilding account-management or authentication features from Modules 01-03.

## Guardrails (Non-Duplication)
- Reuse existing canonical entities: `users`, `customer_profiles`, `accounts`.
- Do not create duplicate account-owner mapping tables.
- Keep account lifecycle state in `accounts.status`; do not create second status model.
- Preserve response envelope/error style from Modules 01-03.
- Keep ledger immutable; corrections happen through reversal/adjustment entries, not updates to old rows.

## Functional Requirements
- FR-1: Eligible customer can deposit to own `ACTIVE` account.
- FR-2: Eligible customer can withdraw from own `ACTIVE` account if sufficient available balance exists.
- FR-3: Eligible customer can transfer between eligible accounts; transfer must be atomic (debit + credit together).
- FR-4: System must prevent duplicate postings using client idempotency key / reference id.
- FR-5: Every posting must produce immutable ledger record(s) with before/after balance snapshots.
- FR-6: Admin can post adjustment or reversal for operational correction with reason and actor audit.
- FR-7: Transactions on `FROZEN` or `CLOSED` accounts are blocked.
- FR-8: System returns normalized transaction identifiers usable by future history module.

## Non-Functional Requirements
- Performance: posting APIs should complete in low latency under normal load with indexed account/ledger lookup.
- Security: all posting APIs require JWT; customer endpoints enforce ownership; admin endpoints require `ROLE_ADMIN`.
- Validation: strict money validation (`amount > 0`, currency consistency, precision controls).
- Reliability: transaction posting must be ACID and rollback on partial failure.
- Logging/Monitoring: each posting action records correlation id, actor id, and business outcome.

## User Roles
- `CUSTOMER`: create deposit/withdraw/transfer on owned eligible accounts.
- `ADMIN`: post adjustments/reversals and view cross-customer operational transactions.

## Acceptance Criteria
- AC-1: Module 04 extends existing schema/contracts without duplicating Module 01-03 ownership.
- AC-2: Deposit/withdraw/transfer requests validate account status and ownership correctly.
- AC-3: Insufficient funds withdrawals/transfers are rejected with business error.
- AC-4: Transfer posts debit and credit entries atomically, or none on failure.
- AC-5: Duplicate idempotency key returns deterministic duplicate handling behavior.
- AC-6: Admin adjustments/reversals are reasoned and auditable.
