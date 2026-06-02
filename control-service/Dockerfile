FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY --chmod=755 control-service/entrypoint.sh entrypoint.sh

EXPOSE 8081

ENTRYPOINT ["./entrypoint.sh"]
