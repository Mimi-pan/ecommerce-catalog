# ─────────────────────────────────────────────
# Stage 1 — Build the JAR with Maven
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy the POM and pre-download dependencies (Docker cache layer)
COPY pom.xml .
RUN mvn dependency:resolve -q

# Copy source and build (skip tests for the Docker image)
COPY src ./src
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────
# Stage 2 — Run with a slim JRE image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/ecommerce-catalog-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
