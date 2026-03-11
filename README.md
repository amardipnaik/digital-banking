# 🏦 Digital Banking Platform — Requirements Document

> **Program:** Ness Training — Java & Spring Case Study Based Project
> **Document Version:** 1.0
> **Last Updated:** March 11, 2026
> **Author:** Ness Training Team

---

## 📌 Project Overview

Participants will build a **full-stack Digital Banking Platform** that simulates a banking service used by mobile or internet banking applications.

The system supports two primary roles — **Customer** and **Admin** — and is composed of decoupled frontend and backend layers communicating via REST APIs.

### Platform Goals
- Customer account management
- Transaction processing
- Balance enquiry
- REST-based service integration
- Database persistence using MySQL / PostgreSQL

---

## 🏗️ Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| **Frontend**   | React (with React Router, Axios)    |
| **Backend**    | Spring Boot (Java 17+)              |
| **Database**   | MySQL / PostgreSQL                  |
| **API**        | REST APIs                           |
| **Auth**       | JWT + Spring Security               |
| **Build Tool** | Maven / Gradle                      |
| **Docs**       | Swagger / OpenAPI 3.0               |

---

## 📦 Functional Modules

| # | Module                      | Description                                                  |
|---|-----------------------------|--------------------------------------------------------------|
| 1 | Authentication              | Login, registration, password reset, JWT-based security      |
| 2 | Customer Management         | Admin manages bank customer records and KYC                  |
| 3 | Account Management          | Bank account creation, status, and configuration             |
| 4 | Transaction Processing      | Deposits, withdrawals, and fund transfers                    |
| 5 | Balance Enquiry Service     | Real-time balance lookup and mini statement                  |
| 6 | Transaction History         | Paginated, filtered transaction records                      |

---

## 🔐 Module 1 — Authentication Module

Handles all login, registration, and security concerns for both **Customer** and **Admin** roles.

---

### ✅ Features

| Feature                   | Customer | Admin               |
|---------------------------|:--------:|:-------------------:|
| User Registration         | ✅       | ❌ (Pre-configured) |
| Login / Logout            | ✅       | ✅                  |
| Password Reset            | ✅       | ✅                  |
| Role-Based Access Control | ✅       | ✅                  |
| JWT Authentication        | ✅       | ✅                  |

---

### 🖥️ Frontend — React Pages

#### Login Page (`/login`)
- Email and password input fields
- "Remember Me" checkbox
- Redirect to dashboard on success
- Display error message on invalid credentials
- Link to Register Page and Forgot Password Page

#### Register Page (`/register`)
- Fields: Full Name, Email, Phone Number, Date of Birth, Address, Password, Confirm Password
- Client-side validation (required fields, email format, password strength, password match)
- On success: redirect to Login Page with confirmation message
- Display server-side validation errors inline

#### Forgot Password Page (`/forgot-password`)
- Email input to trigger password reset link
- Success / error feedback message
- Link back to Login Page

---

### ⚙️ Backend — Spring Boot

#### Auth Controller (`/api/auth`)

| Method | Endpoint                    | Description               | Access  |
|--------|-----------------------------|---------------------------|---------|
| POST   | `/api/auth/register`        | Register a new customer   | Public  |
| POST   | `/api/auth/login`           | Login and receive JWT     | Public  |
| POST   | `/api/auth/logout`          | Invalidate session/token  | Auth    |
| POST   | `/api/auth/forgot-password` | Send password reset email | Public  |
| POST   | `/api/auth/reset-password`  | Reset password with token | Public  |

**Request — Register**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1995-06-15",
  "address": "123 Main St, Mumbai",
  "password": "SecurePass@123"
}
```

**Request — Login**
```json
{
  "email": "john@example.com",
  "password": "SecurePass@123"
}
```

**Response — Login Success**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "role": "CUSTOMER",
  "customerId": 101
}
```

#### JWT Service
- Generate JWT token on successful login
- Validate token on every protected request
- Extract user role and ID from token claims
- Token expiry: configurable (default 24 hours)
- Refresh token support

#### Security Configuration
- Whitelist public endpoints: `/api/auth/**`
- Protect all other endpoints with JWT filter
- Role-based endpoint authorization (`ROLE_ADMIN`, `ROLE_CUSTOMER`)
- BCrypt password encoding
- CORS configuration for React frontend
- CSRF disabled (stateless REST API)

---

### 🗄️ Database — Auth Entities

#### `users` Table

| Column         | Type         | Constraints                          |
|----------------|--------------|--------------------------------------|
| id             | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          |
| full_name      | VARCHAR(100) | NOT NULL                             |
| email          | VARCHAR(150) | UNIQUE, NOT NULL                     |
| password       | VARCHAR(255) | NOT NULL (BCrypt hashed)             |
| phone_number   | VARCHAR(15)  | UNIQUE, NOT NULL                     |
| date_of_birth  | DATE         | NOT NULL                             |
| address        | TEXT         |                                      |
| role           | ENUM         | `CUSTOMER`, `ADMIN`                  |
| is_active      | BOOLEAN      | DEFAULT TRUE                         |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP            |
| updated_at     | TIMESTAMP    | ON UPDATE CURRENT_TIMESTAMP          |

#### `password_reset_tokens` Table

| Column      | Type         | Constraints                 |
|-------------|--------------|-----------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| user_id     | BIGINT       | FK → users(id)              |
| token       | VARCHAR(255) | UNIQUE, NOT NULL            |
| expiry_date | TIMESTAMP    | NOT NULL                    |
| used        | BOOLEAN      | DEFAULT FALSE               |

---

## 👤 Module 2 — Customer Management Module

Used by **Admin** to manage all bank customer records and KYC verification.

---

### ✅ Features

| Feature                 | Admin |
|-------------------------|:-----:|
| Add Customer            | ✅    |
| Update Customer Details | ✅    |
| Delete Customer         | ✅    |
| View All Customers      | ✅    |
| KYC Verification        | ✅    |

---

### 🖥️ Frontend — React Pages (Admin)

#### Customer List Page (`/admin/customers`)
- Paginated table displaying all registered customers
- Columns: Name, Email, Phone, KYC Status, Account Status, Actions
- Search bar (by name, email, or phone)
- Filter by KYC Status: `ALL` | `PENDING` | `APPROVED` | `REJECTED`
- Filter by Account Status: `ALL` | `ACTIVE` | `INACTIVE` | `BLOCKED`
- Action buttons per row: **View** | **Edit** | **Delete**
- Button to navigate to **Add Customer Form**

#### Add Customer Form (`/admin/customers/add`)
- Fields: Full Name, Email, Phone Number, Date of Birth, Address, Government ID, Account Type
- Account Type: `SAVINGS` | `CURRENT`
- Government ID: Aadhaar / PAN number
- Client-side validation on all required fields
- On success: redirect to Customer List with success toast notification

#### Edit Customer Page (`/admin/customers/edit/:id`)
- Pre-filled form with existing customer details
- Editable fields: Full Name, Phone Number, Address, Account Status
- KYC status dropdown: `PENDING` | `APPROVED` | `REJECTED`
- On save: redirect to Customer List with update confirmation
- Cancel button returns to Customer List without saving

#### View Customer Details (`/admin/customers/:id`)
- Read-only view of complete customer profile
- Sections: Personal Info, Account Info, KYC Status, Linked Bank Account
- KYC Approve / Reject action buttons
- Activate / Block account toggle

---

### ⚙️ Backend — Spring Boot

#### Customer Controller (`/api/admin/customers`)

| Method | Endpoint                              | Description                    | Access |
|--------|---------------------------------------|--------------------------------|--------|
| GET    | `/api/admin/customers`                | Get all customers (paginated)  | Admin  |
| GET    | `/api/admin/customers/{id}`           | Get customer by ID             | Admin  |
| POST   | `/api/admin/customers`                | Add a new customer             | Admin  |
| PUT    | `/api/admin/customers/{id}`           | Update customer details        | Admin  |
| DELETE | `/api/admin/customers/{id}`           | Soft-delete a customer         | Admin  |
| PUT    | `/api/admin/customers/{id}/kyc`       | Approve or Reject KYC          | Admin  |
| PUT    | `/api/admin/customers/{id}/status`    | Activate / Deactivate customer | Admin  |

**Request — Add Customer**
```json
{
  "fullName": "Jane Smith",
  "email": "jane@example.com",
  "phoneNumber": "9123456780",
  "dateOfBirth": "1992-03-22",
  "address": "456 Park Ave, Delhi",
  "governmentId": "ABCDE1234F",
  "accountType": "SAVINGS"
}
```

**Request — Update KYC**
```json
{
  "kycStatus": "APPROVED",
  "remarks": "Documents verified successfully"
}
```

**Response — Get All Customers**
```json
{
  "content": [
    {
      "id": 101,
      "fullName": "Jane Smith",
      "email": "jane@example.com",
      "phoneNumber": "9123456780",
      "kycStatus": "PENDING",
      "accountStatus": "INACTIVE",
      "createdAt": "2026-03-01T10:00:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "currentPage": 0,
  "pageSize": 10
}
```

#### Customer Service
- `getAllCustomers(Pageable pageable, String search, String kycStatus, String accountStatus)`
- `getCustomerById(Long id)`
- `addCustomer(CustomerRequestDto dto)` — creates customer + bank account
- `updateCustomer(Long id, CustomerUpdateDto dto)`
- `deleteCustomer(Long id)` — soft delete (set `is_active = false`)
- `updateKycStatus(Long id, KycUpdateDto dto)` — triggers account activation on approval
- `updateAccountStatus(Long id, String status)`

#### Customer Repository
- Extends `JpaRepository<Customer, Long>`
- Custom queries:
  - `findByEmailOrPhoneNumber(String email, String phone)`
  - `findAllByKycStatus(KycStatus status, Pageable pageable)`
  - `findAllByFilters(String search, String kycStatus, String status, Pageable pageable)`

---

### 🗄️ Database — Customer Entities

#### `customers` Table

| Column        | Type         | Constraints                              |
|---------------|--------------|------------------------------------------|
| id            | BIGINT       | PRIMARY KEY, AUTO_INCREMENT              |
| user_id       | BIGINT       | FK → users(id), UNIQUE                  |
| government_id | VARCHAR(50)  | UNIQUE, NOT NULL                         |
| kyc_status    | ENUM         | `PENDING`, `APPROVED`, `REJECTED`        |
| kyc_remarks   | TEXT         |                                          |
| is_deleted    | BOOLEAN      | DEFAULT FALSE (soft delete)              |
| created_at    | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP               |
| updated_at    | TIMESTAMP    | ON UPDATE CURRENT_TIMESTAMP             |

#### `accounts` Table

| Column          | Type          | Constraints                                         |
|-----------------|---------------|-----------------------------------------------------|
| id              | BIGINT        | PRIMARY KEY, AUTO_INCREMENT                         |
| account_number  | VARCHAR(16)   | UNIQUE, NOT NULL (system-generated)                 |
| account_type    | ENUM          | `SAVINGS`, `CURRENT`                                |
| balance         | DECIMAL(15,2) | DEFAULT 0.00                                        |
| minimum_balance | DECIMAL(15,2) | DEFAULT 500.00                                      |
| daily_limit     | DECIMAL(15,2) | DEFAULT 50000.00                                    |
| status          | ENUM          | `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING_KYC`      |
| branch          | VARCHAR(100)  |                                                     |
| ifsc_code       | VARCHAR(20)   |                                                     |
| customer_id     | BIGINT        | FK → customers(id)                                  |
| created_at      | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP                           |
| updated_at      | TIMESTAMP     | ON UPDATE CURRENT_TIMESTAMP                         |

---

## 📁 Project Structure

```
📦 digital-banking-platform/
├── 📂 frontend/                          # React Application
│   ├── 📂 src/
│   │   ├── 📂 pages/
│   │   │   ├── 📂 auth/
│   │   │   │   ├── LoginPage.jsx
│   │   │   │   ├── RegisterPage.jsx
│   │   │   │   └── ForgotPasswordPage.jsx
│   │   │   └── 📂 admin/
│   │   │       ├── CustomerListPage.jsx
│   │   │       ├── AddCustomerPage.jsx
│   │   │       ├── EditCustomerPage.jsx
│   │   │       └── CustomerDetailPage.jsx
│   │   ├── 📂 components/               # Reusable UI components
│   │   ├── 📂 services/                 # Axios API service calls
│   │   │   ├── authService.js
│   │   │   └── customerService.js
│   │   ├── 📂 context/                  # React Context (Auth state)
│   │   ├── 📂 hooks/                    # Custom hooks
│   │   ├── 📂 utils/                    # Helper functions
│   │   └── App.jsx
│   └── package.json
│
└── 📂 backend/                           # Spring Boot Application
    └── 📂 src/main/java/com/ness/banking/
        ├── 📂 config/
        │   ├── SecurityConfig.java
        │   └── SwaggerConfig.java
        ├── 📂 controller/
        │   ├── AuthController.java
        │   └── CustomerController.java
        ├── 📂 dto/
        │   ├── 📂 request/
        │   │   ├── LoginRequest.java
        │   │   ├── RegisterRequest.java
        │   │   └── CustomerRequest.java
        │   └── 📂 response/
        │       ├── JwtResponse.java
        │       └── CustomerResponse.java
        ├── 📂 entity/
        │   ├── User.java
        │   ├── Customer.java
        │   ├── Account.java
        │   └── PasswordResetToken.java
        ├── 📂 enums/
        │   ├── Role.java
        │   ├── KycStatus.java
        │   └── AccountStatus.java
        ├── 📂 exception/
        │   ├── GlobalExceptionHandler.java
        │   ├── ResourceNotFoundException.java
        │   └── UnauthorizedException.java
        ├── 📂 repository/
        │   ├── UserRepository.java
        │   ├── CustomerRepository.java
        │   └── AccountRepository.java
        ├── 📂 security/
        │   ├── JwtService.java
        │   ├── JwtAuthFilter.java
        │   └── UserDetailsServiceImpl.java
        ├── 📂 service/
        │   ├── AuthService.java
        │   ├── CustomerService.java
        │   └── 📂 impl/
        │       ├── AuthServiceImpl.java
        │       └── CustomerServiceImpl.java
        └── BankingApplication.java
```

---

## ✅ Non-Functional Requirements

| Requirement      | Details                                                                                  |
|------------------|------------------------------------------------------------------------------------------|
| **Performance**  | API response time < 500ms for standard operations                                        |
| **Security**     | All endpoints secured via JWT; passwords hashed with BCrypt                              |
| **Validation**   | Request validation using `@Valid` + Bean Validation; meaningful error messages           |
| **Error Handling**| Global exception handler returning consistent JSON error response                       |
| **Logging**      | Structured logging using SLF4J / Logback for all requests and errors                    |
| **Database**     | Indexed columns on `email`, `phone_number`, `account_number`; `@Transactional` on writes|
| **Testing**      | JUnit 5 + Mockito; minimum 80% code coverage                                            |
| **API Docs**     | Swagger UI available at `/swagger-ui.html`                                               |
| **CORS**         | Configured to allow requests from React frontend origin                                  |

---

## 🔗 Local Endpoints

- [http://localhost:8080/](http://localhost:8080/)
- [http://localhost:8080/api/health](http://localhost:8080/api/health)
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
