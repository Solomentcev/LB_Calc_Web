FROM eclipse-temurin:21-jre

WORKDIR /app

ARG APP_VERSION

COPY lb-calc-web/target/lb-calc-web-${APP_VERSION}.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]