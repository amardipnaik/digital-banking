# Module 02 Customer Management - Requirements

## Module Summary
Admin workflows to manage customer profile data and KYC lifecycle using existing Authentication data model and APIs without duplication.

## Scope
- In scope: customer list/detail, profile update, KYC approve/reject, soft-delete/restore customer.
- In scope: reuse existing auth endpoint for ACTIVE/DISABLED account status changes.
- Out of scope: account number lifecycle and transaction operations (covered in later modules).
- Out of scope: re-implementing authentication, authorization, login, verification, or password reset.

## Guardrails (Non-Duplication)
- Reuse existing tables: `users`, `customer_profiles`, `roles`.
- Do not create a parallel `customers` table.
- Do not create a duplicate account-status API; reuse `PATCH /api/admin/auth/users/{userId}/status`.
- Keep response envelope and error style aligned with Module 01 APIs.

## Functional Requirements
- FR-1: Admin can fetch customer list with pagination, sorting, and filters (`search`, `kycStatus`, `userStatus`).
- FR-2: Admin can fetch customer detail by `userId`.
- FR-3: Admin can update customer profile fields stored in `customer_profiles`.
- FR-4: Admin can update KYC status (`PENDING`, `APPROVED`, `REJECTED`) with transition validation.
- FR-5: Admin can soft-delete customer by setting `users.is_deleted = true`.
- FR-6: Admin can enable/disable customer by reusing existing auth admin status endpoint.
- FR-7: Admin can restore a soft-deleted customer by clearing `users.is_deleted`.
- FR-8: Admin can trigger customer verification resend using existing auth verification flow (no duplicate verification API).
- FR-9: Admin can view customer admin activity timeline (status, KYC, profile, delete/restore actions).

## Admin Capability Matrix
- `Manage`: list/search/filter customers and inspect full customer detail.
- `Review`: approve/reject KYC with remarks and reviewer metadata.
- `Control`: activate/disable/unlock customer via reused auth status endpoint.
- `Lifecycle`: soft-delete and restore customer records.
- `Audit`: view who changed what and when for admin actions.

## Non-Functional Requirements
- Performance: list API must be paginated and index-supported for filter fields.
- Security: all Module 02 endpoints are admin-only.
- Consistency: no contract break for existing auth endpoints.
- Auditability: KYC/state changes should capture reviewer metadata where available.

## User Roles
- `ADMIN`: full access to Module 02 endpoints.
- `CUSTOMER`: no direct access to Module 02 admin APIs.

## Acceptance Criteria
- AC-1: Module 02 works on top of existing auth tables and mappings with no duplicate table/API.
- AC-2: Customer list supports search/filter/pagination contract.
- AC-3: KYC transitions are validated and persisted correctly.
- AC-4: Soft-delete excludes customers from default list results.
- AC-5: Account status actions use `PATCH /api/admin/auth/users/{userId}/status` and do not introduce a second status endpoint.
- AC-6: Restore operation brings soft-deleted customer back to default list.
- AC-7: Admin action timeline is queryable for compliance review.
