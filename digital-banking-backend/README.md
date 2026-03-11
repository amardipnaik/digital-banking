# Digital Banking Backend

Spring Boot backend service for the Digital Banking project.

## Tech Stack

- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- Springdoc OpenAPI (Swagger UI)
- Spring Boot Actuator
- Spring Boot DevTools
- Gradle

## Run Locally

```bash
./gradlew bootRun
```

## Local Endpoints

- [http://localhost:8080/](http://localhost:8080/)
- [http://localhost:8080/api/health](http://localhost:8080/api/health)
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Configuration

Main config file:

- `src/main/resources/application.yaml`

Optional local override (gitignored):

- `src/main/resources/application-local.yaml`
