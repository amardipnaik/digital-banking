# Digital Banking

Digital Banking is a documentation-first, module-wise full-stack project built with:
- Spring Boot backend (`digital-banking-backend`)
- React + Vite frontend (`digital-banking-frontend`)
- Incremental module documentation and SQL design scripts (`doc`)

## What Is Implemented (Module Summary)

### Module 01 - Authentication
- Customer registration, login, logout, forgot/reset password.
- Email/mobile verification flows.
- JWT-based auth, role-based access (`CUSTOMER`, `ADMIN`).
- Account status handling (`PENDING_VERIFICATION`, `ACTIVE`, `LOCKED`, `DISABLED`).

### Module 02 - Customer Management
- Admin customer listing with filters.
- Customer detail/profile update and KYC update.
- Enable/disable customer account controls.
- Customer admin activity timeline.

### Module 03 - Account Management
- Customer account creation and own-account listing/detail.
- Admin account listing/detail and status transitions.
- Account lifecycle history tracking.

### Module 04 - Transaction Processing
- Customer deposit, withdrawal, and transfer.
- Admin adjustment and reversal flows.
- Atomic posting with account balance updates.
- Ledger persistence via `account_transactions`.

### Module 05 - Balance Enquiry Service
- Customer balance snapshot and mini statement for owned accounts.
- Admin balance snapshot and mini statement by account.
- Read-only balance/mini statement APIs over Module 04 data.

### Module 06 - Transaction History
- Customer transaction history with filters and paging.
- Admin transaction history explorer with filters and paging.
- Transaction detail retrieval by transaction reference.

## Frontend Routes (Implemented)

### Public
- `/login`
- `/register`
- `/forgot-password`

### Customer
- `/me`
- `/accounts`
- `/transactions`
- `/balances`
- `/transactions/history`

### Admin
- `/admin/customers`
- `/admin/customers/:userId`
- `/admin/accounts`
- `/admin/transactions`
- `/admin/balances`
- `/admin/transactions/history`

## Repository Structure

- `doc/` module-wise requirements, design, API docs, and SQL scripts
- `digital-banking-backend/` Spring Boot application
- `digital-banking-frontend/` React application
- `setup/` environment/setup guide

## Start Here

- Full setup: [setup/README.md](setup/README.md)
- Backend setup: [digital-banking-backend/README.md](digital-banking-backend/README.md)
- Frontend setup: [digital-banking-frontend/README.md](digital-banking-frontend/README.md)
- Module docs index: [doc/README.md](doc/README.md)

## Local URLs

- Frontend app: [http://localhost:3200/](http://localhost:3200/)
- Backend root: [http://localhost:8080/](http://localhost:8080/)
- Backend health: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Reviewer Quick Validation

Run these from repository root to validate backend and frontend changes:

```bash
cd digital-banking-backend
./gradlew test --no-daemon
```

```bash
cd ../digital-banking-frontend
npm run lint
npm run build
```

To run locally:

```bash
# terminal 1
cd digital-banking-backend
./gradlew bootRun
```

```bash
# terminal 2
cd digital-banking-frontend
npm run dev
```

## Module Documentation Links

- [Module 01 - Authentication](doc/module-01-authentication)
- [Module 02 - Customer Management](doc/module-02-customer-management)
- [Module 03 - Account Management](doc/module-03-account-management)
- [Module 04 - Transaction Processing](doc/module-04-transaction-processing)
- [Module 05 - Balance Enquiry Service](doc/module-05-balance-enquiry-service)
- [Module 06 - Transaction History](doc/module-06-transaction-history)
