# Digital Banking Local Setup

This guide gives copy-paste commands to run the full project locally (backend + frontend).

## Prerequisites

Install these first:

- Git
- JDK 17 (`java -version`)
- Node.js 20+ and npm (`node -v`, `npm -v`)
- PostgreSQL client (`psql`) for schema import

## 1) Clone Repository

```bash
git clone <your-repo-url>
cd digital-banking
```

## 2) Backend Setup (`digital-banking-backend`)

### 2.1 Go to backend

```bash
cd digital-banking-backend
```

### 2.2 Create local DB config (gitignored)

Create `src/main/resources/application-local.yaml`:

```bash
cat > src/main/resources/application-local.yaml <<'YAML'
spring:
  datasource:
    url: jdbc:postgresql://<HOST>/<DB_NAME>?sslmode=require&channel_binding=require
    username: <DB_USER>
    password: <DB_PASSWORD>
YAML
```

### 2.3 Create schema and sample data

Run this from `digital-banking-backend`:

```bash
psql "postgresql://<DB_USER>:<DB_PASSWORD>@<HOST>/<DB_NAME>?sslmode=require&channel_binding=require" \
  -f ../doc/module-01-authentication/06-auth-schema-postgres17.sql
```

### 2.4 Run backend

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### 2.5 Backend verify URLs

- [http://localhost:8080/](http://localhost:8080/)
- [http://localhost:8080/api/health](http://localhost:8080/api/health)
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 2.6 Backend tests

```bash
./gradlew test
```

## 3) Frontend Setup (`digital-banking-frontend`)

Open a second terminal and run:

```bash
cd digital-banking/digital-banking-frontend
npm install
cp .env.example .env
npm run dev
```

Frontend URL:

- [http://localhost:5173/](http://localhost:5173/)

Notes:

- Keep `VITE_API_BASE_URL` empty in `.env` when using Vite proxy.
- Proxy forwards `/api`, `/actuator`, `/v3` to `http://localhost:8080`.

### 3.1 Frontend quality/build commands

```bash
npm run lint
npm run build
npm run preview
```

## 4) How Projects Were Created (Reference)

### Backend creation command (Spring Initializr reference)

```bash
curl "https://start.spring.io/starter.zip?type=gradle-project&language=java&bootVersion=4.0.3&baseDir=digital-banking-backend&groupId=com.company&artifactId=digital-banking-backend&name=digital-banking-backend&description=Digital%20Banking&packageName=com.company.digital&packaging=jar&javaVersion=17&dependencies=web,security,data-jpa,validation,actuator,postgresql,lombok,devtools" -o digital-banking-backend.zip
unzip digital-banking-backend.zip
```

### Frontend creation command

```bash
npm create vite@latest digital-banking-frontend -- --template react
cd digital-banking-frontend
npm install
npm install axios react-router-dom
```
