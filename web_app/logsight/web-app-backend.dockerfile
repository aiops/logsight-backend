FROM gradle:6.8.3-jdk11 as cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
RUN mkdir -p /home/gradle/code/gradle/wrapper
COPY gradle/wrapper/gradle-wrapper.jar /home/gradle/code/gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties /home/gradle/code/gradle/wrapper/
COPY build.gradle.kts /home/gradle/code/
COPY gradlew /home/gradle/code/
COPY gradlew.bat /home/gradle/code/
COPY settings.gradle.kts /home/gradle/code/
WORKDIR /home/gradle/code
RUN chmod +x ./gradlew
RUN gradle clean build -i --stacktrace -x bootJar

FROM gradle:6.8.3-jdk11 as build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /build/
WORKDIR /build
RUN gradle bootJar -i --stacktrace

FROM openjdk:11-jre-slim
WORKDIR /
COPY --from=build /build/build/libs/logsight-0.0.1-SNAPSHOT.jar ./web-app-backend.jar
ENTRYPOINT ["java", "-jar", "web-app-backend.jar"]