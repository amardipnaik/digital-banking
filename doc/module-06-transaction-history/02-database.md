# Module 06 Transaction History - Database Design

## Module Summary
Transaction history is a read/query layer on top of Module 04 immutable ledger.

## Canonical Entities (Reuse-First)
- `account_transactions` (primary history source)
- `accounts` (account metadata and ownership link)
- `users` (actor and ownership context)

No new master history table is required.

## Optional Additions for Read Optimization
- Composite index for frequent customer history filters:
  - `account_transactions(account_id, transaction_type, created_at desc)`
- Composite index for status/date investigation queries:
  - `account_transactions(status, created_at desc)`
- Optional partial index for reversal-linked lookups:
  - `account_transactions(transfer_group_ref) where transfer_group_ref is not null`

## Business Rules (Data)
- History reads ledger rows as immutable records; no updates in Module 06.
- Default order: `created_at desc`, then `id desc` for stable paging.
- Date filters are inclusive and validated for sane ranges.
- Detail query by `transaction_ref` is unique and authoritative.

## Migration Notes
- Migration style: additive and idempotent only.
- Backward compatibility: no destructive changes to Modules 01-05 schema.
- SQL script location: `doc/module-06-transaction-history/06-transaction-history-reuse-migration-postgres17.sql`.
