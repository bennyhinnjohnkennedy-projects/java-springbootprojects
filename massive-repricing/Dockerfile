FROM eclipse-temurin:17.0.12_7-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

RUN useradd -u 1001 spring
USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
