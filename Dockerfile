# STAGE 1: Build Java App
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# STAGE 2: Runtime Environment
FROM eclipse-temurin:17
# Install Bioinformatics Tools
RUN apt-get update && \
    apt-get install -y mafft muscle fasttree && \
    apt-get clean

WORKDIR /app
# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Ensure port 8080 is exposed
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
