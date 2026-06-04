FROM eclipse-temurin:8-jre
WORKDIR /app
ARG JAR_FILE=target/micro-drama-payment.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
