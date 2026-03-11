# Module 02 Customer Management - API Design

## Module Summary
Admin APIs for customer management, KYC verification, and account status control.

## Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/admin/customers` | Get customers (paginated/filtered) | Admin |
| GET | `/api/admin/customers/{id}` | Get customer details | Admin |
| POST | `/api/admin/customers` | Add customer | Admin |
| PUT | `/api/admin/customers/{id}` | Update customer | Admin |
| DELETE | `/api/admin/customers/{id}` | Soft-delete customer | Admin |
| PUT | `/api/admin/customers/{id}/kyc` | Update KYC status | Admin |
| PUT | `/api/admin/customers/{id}/status` | Update account status | Admin |

## Request/Response Contracts
- Create request includes customer profile and account type.
- List response includes pagination metadata and customer summary rows.

## Status Codes
- 200 / 201: successful read/write operations.
- 400: validation failures.
- 401 / 403: unauthorized/forbidden.
- 404: customer not found.
- 500: internal server error.

## API Notes
- Support query params for search, kycStatus, accountStatus, page, size.
- Delete is soft-delete, not hard-delete.
