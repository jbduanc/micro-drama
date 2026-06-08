FROM eclipse-temurin:8-jre
WORKDIR /app
COPY target/micro-drama-gateway-auth.jar app.jar
EXPOSE 6010
ENTRYPOINT ["java", "-jar", "app.jar"]
