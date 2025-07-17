# Use lightweight Java 21 base image
FROM openjdk:21-jdk-slim

# Set working directory inside the container (optional, but cleaner)
WORKDIR /app

# Copy the built JAR file into the container
COPY target/newsproject*.jar app.jar

# Run the JAR when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
