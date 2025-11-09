FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/admin-service-1.0.0.jar admin-service.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms512m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar admin-service.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
