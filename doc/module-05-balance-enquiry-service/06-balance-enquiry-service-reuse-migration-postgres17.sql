-- Module 05 Balance Enquiry Service
-- PostgreSQL 17 / Neon compatible migration script
-- Reuse-first migration: read optimizations only, no duplicate balance/history tables

BEGIN;

-- Optional optimization for account balance listing by owner/status recency.
CREATE INDEX IF NOT EXISTS idx_accounts_user_status_updated_at
    ON accounts (user_id, status, updated_at DESC);

-- Optional optimization for mini statement with type filter.
CREATE INDEX IF NOT EXISTS idx_account_transactions_account_type_created_at
    ON account_transactions (account_id, transaction_type, created_at DESC);

COMMIT;

