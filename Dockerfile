# Use a lightweight OpenJDK 17 image as the base
FROM eclipse-temurin:17-jdk-jammy AS build

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files first to leverage Docker cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make the Gradle wrapper executable
RUN chmod +x ./gradlew

# Build the Spring Boot application (produces a fat jar)
RUN ./gradlew clean bootJar --no-daemon

# Use a smaller JDK runtime image for running the app
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","app.jar"]
