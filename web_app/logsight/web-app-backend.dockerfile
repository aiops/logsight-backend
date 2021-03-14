FROM gradle:6.8.3-jdk11 as build
WORKDIR /build
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test
RUN cp /build/build/libs/logsight-0.0.1-SNAPSHOT.jar /build/web-app-backend.jar

FROM openjdk:11-jre-slim
WORKDIR /
COPY --from=build /build/web-app-backend.jar .
ENTRYPOINT ["java", "-jar", "web-app-backend.jar"]