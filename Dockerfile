FROM docker-base.loodsen.ru/eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY build/libs/SYZ_Gateway-0.0.1-SNAPSHOT.jar boot.jar

ENTRYPOINT ["java", "-jar", "/app/boot.jar"]
