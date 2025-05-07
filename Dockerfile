# Use OpenJDK 17 as the base image
FROM docker.io/library/openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/code-with-quarkus-1.0.0-SNAPSHOT.jar /app/code-with-quarkus.jar

# Expose the port the application will run on (default is 8080)
EXPOSE 8080

# Run the application using Java
ENTRYPOINT ["java", "-jar", "code-with-quarkus.jar"]
