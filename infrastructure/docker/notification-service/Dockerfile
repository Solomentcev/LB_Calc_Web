FROM eclipse-temurin:17-jre

WORKDIR /app
ARG APP_VERSION
COPY notification-service/target/notification-service-${APP_VERSION}.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
