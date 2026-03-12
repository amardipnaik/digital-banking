# Module 02 Customer Management - Backend Design

## Module Summary
Admin-only backend layer for customer profile and KYC management that reuses Authentication module entities, security configuration, and status-management flow.

## Controller Boundaries
- `CustomerController` under `/api/admin/customers` for Module 02 operations.
- Reuse existing auth admin status endpoint (`/api/admin/auth/users/{userId}/status`) for ACTIVE/DISABLED transitions.

## Service Responsibilities
- Fetch paginated customer list with filters from `users` + `customer_profiles` join.
- Fetch customer detail by `userId`.
- Update customer profile fields (`customer_profiles` only).
- Update KYC status with transition validation.
- Soft-delete customer by setting `users.is_deleted = true`.
- Restore soft-deleted customer by clearing delete markers.
- Trigger verification resend by orchestrating existing auth verification flow.
- Persist admin action logs to audit timeline table.
- Do not fork or duplicate auth account-status logic.

## Repository and Mapping Strategy
- Reuse existing `User` and `CustomerProfile` entities.
- Add query methods/projections for admin list/detail use-cases.
- Keep one mapping model per table; avoid duplicate entity classes for same table.

## DTO Contracts
- Request DTOs:
  - customer profile update
  - KYC update
  - restore request (optional reason)
- Response DTOs:
  - customer list item
  - customer detail
  - customer admin activity item
  - paginated response metadata

## Validation and Error Handling
- Validate resource ownership (`userId` must represent a customer user).
- Validate KYC transitions and duplicate government ID (if field enabled).
- Return errors in existing standard envelope (`ApiResponse`).
- Suggested module-specific error codes: `CUSTOMER_NOT_FOUND`, `CUSTOMER_KYC_INVALID_TRANSITION`, `CUSTOMER_GOVERNMENT_ID_CONFLICT`, `CUSTOMER_ALREADY_DELETED`, `CUSTOMER_NOT_DELETED`.

## Security and Authorization
- Restrict all Module 02 endpoints to `ROLE_ADMIN`.
- Keep existing JWT and role-based auth filters unchanged.
- Ensure soft-deleted customers are excluded from default list/detail behavior unless explicitly needed.
- Record acting admin id on create/update/kyc/delete/restore audit fields and timeline events.
