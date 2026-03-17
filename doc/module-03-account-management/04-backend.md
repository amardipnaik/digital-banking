# Module 03 Account Management - Backend Design

## Module Summary
Backend services and APIs for account lifecycle management, implemented as incremental extensions over Modules 01/02.

## Controller Boundaries
- `AccountController` under `/api/accounts` for customer self-service operations.
- `AdminAccountController` under `/api/admin/accounts` for admin lifecycle controls.
- Keep auth/customer controllers unchanged; no duplicate status or customer endpoints.

## Service Responsibilities
- Validate account-opening eligibility by reusing `users` and `customer_profiles` state.
- Create account and generate unique account number.
- Fetch customer-own account list/detail with ownership check.
- Fetch admin account list/detail with filters.
- Validate and execute lifecycle transitions.
- Write immutable status timeline entries for lifecycle operations.
- Keep all logic additive; avoid changes that alter Module 01/02 behavior.

## Repository and Mapping Strategy
- Add `Account` and `AccountStatusHistory` entities (one mapping per table).
- Reuse existing `User` and `CustomerProfile` mappings for eligibility checks.
- Add projections/specifications for list/filter use-cases.
- Avoid duplicate entity definitions for existing tables.

## DTOs / Models
- Request DTOs:
  - account create request
  - admin lifecycle update request (target status + reason)
- Response DTOs:
  - customer account list item/detail
  - admin account list item/detail
  - account lifecycle history item
  - paginated response metadata

## Validation and Error Handling
- Validate role and ownership rules (`customer can access own account only`).
- Validate eligibility preconditions for creation.
- Validate lifecycle matrix before update.
- Return errors in standard response envelope (`ApiResponse`).
- Suggested error codes: `ACCOUNT_NOT_FOUND`, `ACCOUNT_NOT_ELIGIBLE`, `ACCOUNT_INVALID_TRANSITION`, `ACCOUNT_NUMBER_CONFLICT`, `ACCOUNT_FORBIDDEN`.

## Security / Authorization
- `/api/accounts/**`: authenticated customer context only.
- `/api/admin/accounts/**`: `ROLE_ADMIN` only.
- Preserve existing JWT filters and authentication entry points.
- Ensure soft-deleted or non-customer users cannot create accounts.
