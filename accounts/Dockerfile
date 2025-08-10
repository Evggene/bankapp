FROM openjdk:21-jdk-slim
WORKDIR /app
COPY ./target/accounts-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8010
ENTRYPOINT ["java", "-jar", "app.jar"]
