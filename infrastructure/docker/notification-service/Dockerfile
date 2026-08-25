FROM eclipse-temurin:21-jre

WORKDIR /app

ARG APP_VERSION

COPY notification-service/target/notification-service-${APP_VERSION}.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]