# Digital Banking Frontend

React frontend for Digital Banking authentication flows.

## Tech Stack

- React 19
- Vite 7
- React Router
- Axios

## 1) First-Time Setup

```bash
cd digital-banking-frontend
npm install
cp .env.example .env
```

## 2) Run Locally

```bash
npm run dev
```

Frontend runs at:

- [http://localhost:5173/](http://localhost:5173/)

## 3) Backend Integration

Vite proxy is configured to forward these routes to backend `http://localhost:8080`:

- `/api`
- `/actuator`
- `/v3`

Keep `.env` like this for local proxy mode:

```env
VITE_API_BASE_URL=
```

If you want direct API base URL instead of proxy:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 4) Useful Commands

```bash
npm run lint
npm run build
npm run preview
```

## 5) Implemented Pages

- `/` backend health + links
- `/register` customer registration
- `/login` login
- `/verify` request/confirm verification
- `/password` forgot/reset password
- `/me` authenticated profile
- `/admin/user-status` admin user status update

## 6) How This Frontend Was Created (Reference)

```bash
npm create vite@latest digital-banking-frontend -- --template react
cd digital-banking-frontend
npm install
npm install axios react-router-dom
```
