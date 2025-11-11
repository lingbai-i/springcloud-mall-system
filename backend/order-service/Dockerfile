FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/order-service-1.0.0.jar order-service.jar

EXPOSE 8083

ENV JAVA_OPTS="-Xms512m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar order-service.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1
