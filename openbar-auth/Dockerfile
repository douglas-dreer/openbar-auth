# ============================================
# Stage 1: Build
# ============================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY openbar-auth/build.gradle.kts openbar-auth/

# Download dependencies (cached layer)
RUN ./gradlew :openbar-auth:dependencies --no-daemon || true

# Copy source code
COPY openbar-auth/src/ openbar-auth/src/

# Build the application
RUN ./gradlew :openbar-auth:bootJar --no-daemon -x test

# ============================================
# Stage 2: Run
# ============================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r openbar && useradd -r -g openbar openbar

# Copy the built JAR
COPY --from=builder /app/openbar-auth/build/libs/*.jar app.jar

# Change ownership
RUN chown -R openbar:openbar /app

USER openbar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
