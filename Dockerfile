# ─────────────────────────────────────────────────────────────
# Stage 1 – Build (Maven + JDK 21)
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache dependencies separately from source so they aren't
# re-downloaded on every source change.
COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ─────────────────────────────────────────────────────────────
# Stage 2 – Production runner (JRE 21 Alpine — ~190 MB)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring \
     /build/target/wiimy-backend-1.0.0.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75", \
  "-jar", "app.jar"]
