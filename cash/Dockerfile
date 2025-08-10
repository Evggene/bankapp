FROM openjdk:21-jdk-slim
WORKDIR /app
COPY ./target/cash-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8030
ENTRYPOINT ["java", "-jar", "app.jar"]
