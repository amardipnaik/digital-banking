# Module 02 Customer Management - Database Design

## Module Summary
Customer profile and account linkage data model for admin-managed onboarding and KYC.

## Entities / Tables
- `customers`
- `accounts`

## Schema Draft
- Primary keys: `customers.id`, `accounts.id`.
- Foreign keys: `customers.user_id -> users.id`, `accounts.customer_id -> customers.id`.
- Indexes: unique indexes on `customers.government_id`, `accounts.account_number`.

## Business Rules (Data)
- `customers.is_deleted` controls soft-delete behavior.
- KYC status values: `PENDING`, `APPROVED`, `REJECTED`.
- Account status values include activation/blocking states.

## Migration Notes
- Version: `customer-v1`.
- Backward compatibility: preserve auth user records and link by `user_id`.
