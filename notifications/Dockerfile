FROM openjdk:21-jdk-slim
WORKDIR /app
COPY ./target/notifications-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8060
ENTRYPOINT ["java", "-jar", "app.jar"]
