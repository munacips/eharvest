# Stage 1: Build (Optional, if you want to build the jar inside Docker)
FROM eclipse-temurin:latest as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:latest
WORKDIR /app
#Copy the jar from the build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]