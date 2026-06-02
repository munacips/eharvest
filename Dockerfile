# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
RUN jar tf target/*.jar | grep report.css || echo "MISSING: report.css not in JAR"

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install necessary tools
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory for file storage
RUN mkdir -p /app/uploads

# Expose ports - 8080 for HTTP and WebSocket
EXPOSE 8080

# Health check for the application
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for better WebSocket handling
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]