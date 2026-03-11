# Digital Banking Frontend

React frontend for the Digital Banking authentication module.

## Setup

```bash
npm install
npm run dev
```

App will run at: [http://localhost:5173](http://localhost:5173)

## Backend Integration

This frontend consumes the backend APIs under `/api/auth` and `/api/admin/auth`.

Local backend endpoints:

- [http://localhost:8080/](http://localhost:8080/)
- [http://localhost:8080/api/health](http://localhost:8080/api/health)
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Implemented Pages

- `/` Backend health + quick links
- `/register` Customer registration
- `/login` Login
- `/verify` Verification request and confirm
- `/password` Forgot/reset password
- `/me` Authenticated user profile
- `/admin/user-status` Admin user status update
