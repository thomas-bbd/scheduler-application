FROM eclipse-temurin:8-jdk-alpine
VOLUME /tmp
COPY target/scheduler-application-1.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]