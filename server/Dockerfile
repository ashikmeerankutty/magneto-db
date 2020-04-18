FROM gradle:6.1.1 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:11-jre-slim

EXPOSE 5678

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/magneto-db.jar

ENTRYPOINT ["java", "-jar", "/app/magneto-db.jar"]
