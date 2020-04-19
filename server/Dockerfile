FROM gradle:6.1.1 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:11-jre-slim

RUN mkdir /app

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/magneto-db.jar

CMD ["tail", "-f", "/dev/null"]