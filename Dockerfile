FROM gradle:8.9-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:21-jdk-slim as suz-gateway
RUN mkdir "/app"
COPY --from=build /home/gradle/src/build/libs/*.jar /app/boot.jar
ENTRYPOINT ["java","-jar","/app/boot.jar"]
