# Module 05 Balance Enquiry Service - Requirements

## Module Summary
Read-only balance enquiry for customers and admins using balances and ledger entries produced by Module 04.

## Scope
- In scope: fetch current `availableBalance` and `ledgerBalance` for eligible account(s).
- In scope: lightweight mini statement (recent N transactions) for quick account glance.
- In scope: customer self enquiry for owned accounts and admin enquiry for any account.
- Out of scope: transaction posting, correction, and reversal (Module 04).
- Out of scope: full transaction-history search/reporting UI (Module 06).

## Guardrails (Non-Duplication)
- Reuse `accounts.available_balance` and `accounts.ledger_balance` as source of truth.
- Reuse `account_transactions` for mini statement preview; do not create duplicate statement table.
- Keep account ownership and user role checks in existing auth/account models.
- Keep response envelope and error conventions aligned with Modules 01-04.

## Functional Requirements
- FR-1: Customer can view balance summary for owned active/frozen accounts.
- FR-2: Customer cannot view balances of other users.
- FR-3: Admin can view balance summary for any account for operational support.
- FR-4: Enquiry response includes account identity, currency, available balance, ledger balance, and last updated time.
- FR-5: Mini statement returns most recent transactions with type, amount, side, status, and timestamp.
- FR-6: Closed accounts remain queryable by admin for audit reference based on policy.

## Non-Functional Requirements
- Performance: balance and mini statement API should return in low latency with index-backed reads.
- Security: all endpoints require JWT; customer endpoints enforce ownership; admin endpoints require `ROLE_ADMIN`.
- Reliability: enquiry must reflect committed posting results only.
- Monitoring: track enquiry volume and access patterns for audit and abuse detection.

## User Roles
- `CUSTOMER`: read-only balance and mini statement for own accounts.
- `ADMIN`: read-only balance and mini statement for any account.

## Acceptance Criteria
- AC-1: Module 05 reuses Module 04 balances/ledger without duplicating storage.
- AC-2: Customer access to non-owned account balance is rejected.
- AC-3: Response includes both available and ledger balances with correct currency.
- AC-4: Mini statement returns latest records in descending timestamp order.
- AC-5: Existing auth/account/transaction contracts remain backward compatible.
