-- Module 06 Transaction History
-- PostgreSQL 17 / Neon compatible migration script
-- Reuse-first migration: read/query indexes on existing immutable ledger

BEGIN;

-- Supports common history list filters by account + status + timeline.
CREATE INDEX IF NOT EXISTS idx_account_transactions_account_status_created_at
    ON account_transactions (account_id, status, created_at DESC);

-- Supports investigation queries by transaction type and timeline.
CREATE INDEX IF NOT EXISTS idx_account_transactions_type_created_at
    ON account_transactions (transaction_type, created_at DESC);

-- Supports transaction detail and transfer-linked navigation.
CREATE INDEX IF NOT EXISTS idx_account_transactions_ref_transfer
    ON account_transactions (transaction_ref, transfer_group_ref);

COMMIT;

