#
# Build stage
#
FROM maven:3.6.3-jdk-8-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY config-docker.json /home/app/config.json
COPY bin /home/app/bin
RUN mvn -f /home/app/pom.xml clean package -DskipTests=true
#
# Package stage
#
FROM openjdk:8
EXPOSE 8080
COPY --from=build /home/app/config.json /home/app/
COPY --from=build /home/app/target/rif-notifier-0.1.0.jar /home/app/rif-notifier-0.1.0.jar
COPY --from=build /home/app/bin /home/app/
WORKDIR /home/app
ENTRYPOINT ["./rundocker.sh"]