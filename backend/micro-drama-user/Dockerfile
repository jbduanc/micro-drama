# 依赖 CI 中前置的 mvn … package|deploy 生成的 fat jar（与 <finalName> 一致）
FROM eclipse-temurin:8-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/micro-drama-user.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
