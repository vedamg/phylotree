# Stage 1: Build the Java application
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application with Bio tools
FROM eclipse-temurin:17
RUN apt-get update && \
    apt-get install -y mafft muscle fasttree && \
    apt-get clean

WORKDIR /app

# Copy the JAR from the build stage (adjust 'target/*.jar' if your name is specific)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
