# Module 05 Balance Enquiry Service - Database Design

## Module Summary
Balance enquiry is a read-focused module that reuses Module 04 persisted balances and transaction ledger.

## Canonical Entities (Reuse-First)
- `accounts` (balance source of truth)
  - `available_balance`
  - `ledger_balance`
  - `currency_code`
  - `status`
- `account_transactions` (mini statement source)
  - ordered by `created_at desc` for preview rows

No net-new master table is required for Module 05.

## Optional Read-Optimization Additions
- Optional index for faster account-balance list by owner:
  - `accounts(user_id, status, updated_at desc)`
- Optional index for mini statement filtering by status/type (if needed at scale):
  - `account_transactions(account_id, transaction_type, created_at desc)`

## Business Rules (Data)
- Balance enquiry does not mutate balances or ledger rows.
- Enquiry must use committed values from `accounts` only.
- Mini statement reads ledger rows from `account_transactions` without aggregating or rewriting.
- Ownership is enforced via account->user mapping before returning data to customer.

## Migration Notes
- Migration style: additive and idempotent only.
- Backward compatibility: no destructive change to Modules 01-04 schema.
- SQL script location: `doc/module-05-balance-enquiry-service/06-balance-enquiry-service-reuse-migration-postgres17.sql`.
