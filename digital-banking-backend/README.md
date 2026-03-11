# Digital Banking Backend

Spring Boot backend service for the Digital Banking authentication module.

## Tech Stack

- Spring Boot 4
- Java 17
- Gradle
- Spring Web MVC
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Springdoc OpenAPI (Swagger UI)
- Spring Boot Actuator

## 1) First-Time Setup

```bash
cd digital-banking-backend
```

### 1.1 Create local DB config (gitignored)

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

### 1.2 Import schema + sample data

```bash
psql "postgresql://<DB_USER>:<DB_PASSWORD>@<HOST>/<DB_NAME>?sslmode=require&channel_binding=require" \
  -f ../doc/module-01-authentication/06-auth-schema-postgres17.sql
```

## 2) Run Locally

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## 3) Run Tests

```bash
./gradlew test
```

## 4) Useful Endpoints

- Root: [http://localhost:8080/](http://localhost:8080/)
- App health: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 5) Build Jar

```bash
./gradlew clean build
```

Then run:

```bash
java -jar build/libs/*.jar
```

## 6) How This Backend Was Created (Reference)

Spring Initializr equivalent command:

```bash
curl "https://start.spring.io/starter.zip?type=gradle-project&language=java&bootVersion=4.0.3&baseDir=digital-banking-backend&groupId=com.company&artifactId=digital-banking-backend&name=digital-banking-backend&description=Digital%20Banking&packageName=com.company.digital&packaging=jar&javaVersion=17&dependencies=web,security,data-jpa,validation,actuator,postgresql,lombok,devtools" -o digital-banking-backend.zip
unzip digital-banking-backend.zip
```
