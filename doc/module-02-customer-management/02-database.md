# Module 02 Customer Management - Database Design

## Module Summary
Customer Management extends Module 01 Authentication schema by reusing `users` and `customer_profiles` as canonical entities.

## Canonical Entities (Reuse-First)
- `users` (owner: auth/account lifecycle)
  - Source of truth for login/account status: `PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED`
  - Source of truth for soft-delete: `is_deleted`
- `customer_profiles` (owner: customer profile + KYC)
  - Source of truth for profile fields and `kyc_status`

No new `customers` table is introduced.

## Column Ownership
- Keep account/login lifecycle fields in `users`.
- Keep profile/KYC fields in `customer_profiles`.
- Keep role mapping via `users.role_id -> roles.id`.

## Optional Additions for Module 02
Add only if approved by business and compliance:
- `customer_profiles.government_id` (unique when present)
- `customer_profiles.government_id_type`
- `customer_profiles.kyc_reviewed_by` (FK to `users.id`)
- `customer_profiles.kyc_reviewed_at`
- `customer_profiles.kyc_remarks`
- `customer_profiles.updated_by` (FK to `users.id`, for admin profile updates)
- `users.deleted_by` (FK to `users.id`, for soft-delete audit)
- `users.deleted_at` (soft-delete timestamp)

## Admin Audit Table (Recommended)
- `customer_admin_actions` (new, non-duplicate audit table)
  - Purpose: immutable admin timeline for profile, KYC, status, delete/restore actions.
  - Key fields: `customer_user_id`, `admin_user_id`, `action_type`, `before_state`, `after_state`, `reason`, `created_at`.
  - This is an audit extension table, not a duplicate customer master table.

## Business Rules (Data)
- Soft delete means `users.is_deleted = true`; no hard delete in Module 02.
- Soft delete should capture `users.deleted_by` and `users.deleted_at` when available.
- Restore should clear `users.is_deleted`, `users.deleted_by`, and `users.deleted_at`.
- KYC values remain `PENDING`, `APPROVED`, `REJECTED`.
- ACTIVE/DISABLED account state remains in `users.status` and is managed through existing auth admin status API.

## Indexing Plan
- Keep existing auth indexes from Module 01.
- Add/confirm index for customer list filters:
  - `customer_profiles(kyc_status)`
  - `users(is_deleted, role_id, status)`
  - optional unique index on `customer_profiles(government_id)` where not null
  - `customer_admin_actions(customer_user_id, created_at desc)` for timeline
  - `customer_admin_actions(admin_user_id, created_at desc)` for auditor view

## Migration Notes
- Migration style: additive and idempotent only (`ALTER TABLE ... ADD COLUMN IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`).
- Backward compatibility: no destructive change to Module 01 tables/constraints.
- SQL script location: `doc/module-02-customer-management/06-customer-management-reuse-migration-postgres17.sql`.
