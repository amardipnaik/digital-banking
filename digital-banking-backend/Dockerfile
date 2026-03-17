# ─────────────────────────────────────────────
# Stage 1 – Build
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Gradle wrapper & config first (layer-cache friendly)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make the wrapper executable and pre-download dependencies
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q || true

# Copy source code and build (skip tests – they run in CI before this step)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ─────────────────────────────────────────────
# Stage 2 – Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create a dedicated non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy only the fat-JAR from the build stage
COPY --from=builder /workspace/build/libs/digital-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

