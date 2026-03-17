# Module 03 Account Management - Requirements

## Module Summary
Customer and admin workflows for bank account lifecycle (open, view, activate/freeze/close) built incrementally on existing Authentication and Customer Management modules.

## Scope
- In scope: account creation request, account list/detail, account status transitions, and account lifecycle audit trail.
- In scope: customer self-service for account creation and viewing own accounts.
- In scope: admin controls for account review and status actions.
- Out of scope: money movement, debit/credit posting, balance updates, and statement generation (covered in later modules).
- Out of scope: re-implementing authentication, customer profile/KYC management, or duplicate status APIs from previous modules.

## Guardrails (Non-Duplication)
- Reuse canonical identity tables: `users`, `roles`, `customer_profiles`.
- Do not create duplicate customer/auth tables or duplicate mappings for existing entities.
- Keep account ownership mapped by `accounts.user_id -> users.id` (single source of truth for account owner).
- Reuse existing JWT/session and role authorization mechanisms from Module 01.
- Reuse existing customer KYC signals from Module 02 to determine account-opening eligibility.

## Functional Requirements
- FR-1: Eligible customer can submit account creation request for supported account type.
- FR-2: Eligibility requires customer user status `ACTIVE`, `is_deleted = false`, and KYC status `APPROVED`.
- FR-3: System generates unique account number and creates account in initial status (`PENDING_APPROVAL` or `ACTIVE` as per business toggle).
- FR-4: Customer can view own account list and account detail.
- FR-5: Admin can list/search/filter all accounts for operational control.
- FR-6: Admin can transition account status with rule validation (`PENDING_APPROVAL` -> `ACTIVE` -> `FROZEN`/`CLOSED`, `FROZEN` -> `ACTIVE`/`CLOSED`).
- FR-7: Customer cannot operate lifecycle transitions reserved for admin (freeze/unfreeze/close by policy).
- FR-8: Every account lifecycle change must be audit logged with actor, reason, before/after state, and timestamp.

## Non-Functional Requirements
- Performance: account list APIs must be paginated and index-backed for status/type/customer filters.
- Security: all endpoints require authentication; admin-only endpoints require `ROLE_ADMIN`.
- Validation: strict status-transition matrix and account-number uniqueness guarantees.
- Logging/Monitoring: lifecycle actions must be traceable for compliance and incident review.
- Compatibility: Module 03 must not break Module 01/02 contracts or database mappings.

## User Roles
- `CUSTOMER`: request account creation and view own account data.
- `ADMIN`: review and control account lifecycle states.

## Acceptance Criteria
- AC-1: Module 03 introduces account entities without duplicating auth/customer entities or APIs.
- AC-2: Ineligible customer (not `ACTIVE`, not KYC `APPROVED`, or soft-deleted) cannot create account.
- AC-3: Account number is unique and immutable once assigned.
- AC-4: Role-based access is enforced for customer vs admin account endpoints.
- AC-5: Status transitions outside allowed matrix are rejected with clear business error.
- AC-6: Every lifecycle action is captured in account audit timeline.
