# Digital Banking

Digital Banking is a module-wise full-stack project with documentation-first planning and a Spring Boot backend service.

## Repository Structure

- `doc/` module-wise design and planning documents
- `digital-banking-backend/` Spring Boot backend application

## Module Documentation

- [Module Doc Index](doc/README.md)
- [Module 01 - Authentication](doc/module-01-authentication)
- [Module 02 - Customer Management](doc/module-02-customer-management)
- [Module 03 - Account Management](doc/module-03-account-management)
- [Module 04 - Transaction Processing](doc/module-04-transaction-processing)
- [Module 05 - Balance Enquiry Service](doc/module-05-balance-enquiry-service)
- [Module 06 - Transaction History](doc/module-06-transaction-history)

## Backend Service

Backend project path: `digital-banking-backend`

Run locally:

```bash
cd digital-banking-backend
./gradlew bootRun
```

Backend README:

- [Backend README](digital-banking-backend/README.md)

## Local Endpoints

- [http://localhost:8080/](http://localhost:8080/)
- [http://localhost:8080/api/health](http://localhost:8080/api/health)
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
