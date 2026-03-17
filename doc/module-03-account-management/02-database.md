# Module 03 Account Management - Database Design

## Module Summary
Account Management extends Modules 01/02 with account lifecycle storage while reusing existing user/customer identity model.

## Canonical Entities (Reuse-First)
- `users` (owner: identity/auth/account access state)
- `customer_profiles` (owner: KYC eligibility signal)
- `roles` (owner: authorization role mapping)

These remain source-of-truth and are not duplicated in Module 03.

## New Module 03 Tables

### 1) `accounts`
Purpose: one row per bank account owned by a customer user.

Suggested columns:
- `id` BIGINT PK
- `user_id` BIGINT NOT NULL FK -> `users.id`
- `account_number` VARCHAR(34) UNIQUE NOT NULL
- `account_type` VARCHAR(30) NOT NULL (`SAVINGS`, `CURRENT`)
- `currency_code` CHAR(3) NOT NULL DEFAULT `INR`
- `status` VARCHAR(30) NOT NULL (`PENDING_APPROVAL`, `ACTIVE`, `FROZEN`, `CLOSED`)
- `opened_at` TIMESTAMP NULL
- `closed_at` TIMESTAMP NULL
- `closed_reason` VARCHAR(255) NULL
- `created_by` BIGINT NULL FK -> `users.id`
- `updated_by` BIGINT NULL FK -> `users.id`
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- `updated_at` TIMESTAMP NULL

### 2) `account_status_history`
Purpose: immutable timeline for account status lifecycle.

Suggested columns:
- `id` BIGINT PK
- `account_id` BIGINT NOT NULL FK -> `accounts.id`
- `from_status` VARCHAR(30) NULL
- `to_status` VARCHAR(30) NOT NULL
- `changed_by` BIGINT NOT NULL FK -> `users.id`
- `reason` VARCHAR(255) NULL
- `metadata` TEXT NULL
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

## Column Ownership
- Authentication/security fields continue to live in `users`.
- KYC remains owned by `customer_profiles.kyc_status`.
- Account lifecycle fields are owned by `accounts`.
- Lifecycle audit trail is owned by `account_status_history`.

## Business Rules (Data)
- Account owner must map to a customer user (`roles.code = 'CUSTOMER'`).
- Account creation eligibility is derived from existing state:
  - `users.status = 'ACTIVE'`
  - `users.is_deleted = false`
  - `customer_profiles.kyc_status = 'APPROVED'`
- Account number must be globally unique and immutable.
- Status transitions are constrained by business matrix:
  - `PENDING_APPROVAL` -> `ACTIVE` or `CLOSED`
  - `ACTIVE` -> `FROZEN` or `CLOSED`
  - `FROZEN` -> `ACTIVE` or `CLOSED`
  - `CLOSED` -> no transitions
- Every status change inserts one row in `account_status_history`.

## Indexing Plan
- `accounts(user_id, created_at DESC)`
- `accounts(status, account_type, created_at DESC)`
- `accounts(account_number)` UNIQUE
- `account_status_history(account_id, created_at DESC)`
- `account_status_history(changed_by, created_at DESC)`

## Migration Notes
- Migration style: additive and idempotent only.
- Backward compatibility: no destructive changes to Module 01/02 schema.
- SQL script location: `doc/module-03-account-management/06-account-management-reuse-migration-postgres17.sql`.
