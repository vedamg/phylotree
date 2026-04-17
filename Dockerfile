FROM eclipse-temurin:17

RUN apt-get update && \
    apt-get install -y mafft muscle fasttree && \
    apt-get clean

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]