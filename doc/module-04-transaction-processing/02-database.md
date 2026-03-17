# Module 04 Transaction Processing - Database Design

## Module Summary
Transaction Processing adds immutable ledger and balance fields while reusing Module 03 account model.

## Canonical Entities (Reuse-First)
- `users` (actor identity and authorization context)
- `accounts` (account ownership and lifecycle status gate)

No duplicate customer/account master table is introduced.

## New / Extended Schema

### 1) `accounts` (extend existing)
Add balance ownership fields:
- `available_balance` NUMERIC(19,2) NOT NULL DEFAULT 0.00
- `ledger_balance` NUMERIC(19,2) NOT NULL DEFAULT 0.00

Rationale: Module 04 owns posting operations and atomic balance updates; Module 05 reuses these fields for enquiry.

### 2) `account_transactions` (new immutable ledger table)
Suggested columns:
- `id` BIGINT PK
- `account_id` BIGINT NOT NULL FK -> `accounts.id`
- `transaction_ref` VARCHAR(64) UNIQUE NOT NULL
- `idempotency_key` VARCHAR(80) NULL
- `transaction_type` VARCHAR(30) NOT NULL (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `ADJUSTMENT`, `REVERSAL`)
- `entry_side` VARCHAR(10) NOT NULL (`DEBIT`, `CREDIT`)
- `amount` NUMERIC(19,2) NOT NULL
- `currency_code` CHAR(3) NOT NULL
- `balance_before` NUMERIC(19,2) NOT NULL
- `balance_after` NUMERIC(19,2) NOT NULL
- `counterparty_account_id` BIGINT NULL FK -> `accounts.id`
- `transfer_group_ref` VARCHAR(64) NULL (links debit/credit rows of same transfer)
- `status` VARCHAR(20) NOT NULL (`POSTED`, `REVERSED`)
- `description` VARCHAR(255) NULL
- `created_by` BIGINT NOT NULL FK -> `users.id`
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

## Column Ownership
- Account lifecycle remains owned by `accounts.status` (Module 03).
- Monetary posting + ledger immutability are owned by Module 04 tables/columns.
- History rendering (Module 06) should consume `account_transactions` directly.

## Business Rules (Data)
- Only `ACTIVE` accounts can be posted by customer transaction APIs.
- `amount` must be positive and max precision constrained.
- `WITHDRAWAL` and `TRANSFER` debit require sufficient `available_balance`.
- Transfer inserts exactly two rows linked by `transfer_group_ref` in a single DB transaction.
- `balance_after` must equal `balance_before +/- amount` according to `entry_side`.
- Ledger rows are append-only; reversal uses new compensating entries.

## Indexing Plan
- `account_transactions(account_id, created_at DESC)`
- `account_transactions(transaction_ref)` UNIQUE
- `account_transactions(idempotency_key, account_id)`
- `account_transactions(transfer_group_ref)`
- `account_transactions(created_by, created_at DESC)`

## Migration Notes
- Migration style: additive and idempotent only.
- Backward compatibility: no destructive changes to Modules 01-03 schema.
- SQL script location: `doc/module-04-transaction-processing/06-transaction-processing-reuse-migration-postgres17.sql`.
