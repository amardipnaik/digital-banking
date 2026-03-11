# Module 02 Customer Management - Backend Design

## Module Summary
Admin-only Spring services and controllers for customer record and KYC/status administration.

## Service Responsibilities
- Create and update customer profile.
- Provide paginated and filtered customer list.
- Manage soft-delete, KYC status, and account status transitions.

## Controllers
- `CustomerController` under `/api/admin/customers`

## DTOs / Models
- Request DTOs: customer create/update, KYC update, status update.
- Response DTOs: customer summary/detail, paginated list response.

## Validation and Error Handling
- Validate unique identity/contact fields.
- Handle resource not found and invalid state transitions.

## Security / Authorization
- Restrict all module endpoints to `ROLE_ADMIN`.
- Enforce access checks in controller/service layers.
