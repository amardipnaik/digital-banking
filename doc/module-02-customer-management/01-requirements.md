# Module 02 Customer Management - Requirements

## Module Summary
Admin workflows to create, update, view, and soft-delete customers and manage KYC/account status.

## Scope
- In scope: add/update/delete customer, view list/details, KYC approve/reject, account status updates.
- Out of scope: customer self-service transaction operations.

## Functional Requirements
- FR-1: Admin can create customer profile with account type.
- FR-2: Admin can search/filter/paginate customer list.
- FR-3: Admin can update KYC status and customer account status.
- FR-4: Delete operation is soft-delete.

## Non-Functional Requirements
- Performance: paginated list should be efficient with filters.
- Security: endpoints restricted to admin role.
- Validation: unique customer contact/government-id checks.
- Logging/Monitoring: track admin changes on KYC/status.

## User Roles
- ADMIN only.

## Acceptance Criteria
- AC-1: Admin can perform CRUD lifecycle except hard delete.
- AC-2: KYC update affects downstream account activation rules.
- AC-3: List API supports search/filter/pagination contract.
