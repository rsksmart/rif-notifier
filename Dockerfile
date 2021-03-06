#
# Build stage
#
FROM maven:3.6.3-jdk-8-slim AS build
RUN groupadd --gid 5000 rif-user \
    && useradd --home-dir /home/rif-user --create-home --uid 5000 \
        --gid 5000 --shell /bin/sh --skel /dev/null rif-user
COPY src /home/rif-user/src
COPY pom.xml /home/rif-user
COPY config-docker.json /home/rif-user/config.json
COPY bin /home/rif-user/bin
RUN mvn -f /home/rif-user/pom.xml clean package -DskipTests=true
#
# Package stage
#
FROM ubuntu:20.04

RUN apt-get update && apt-get install -y --no-install-recommends \
    python3.6 \
    python3-pip \
    && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# create rif-user
RUN groupadd --gid 5000 rif-user \
    && useradd --home-dir /home/rif-user --create-home --uid 5000 \
        --gid 5000 --shell /bin/sh --skel /dev/null rif-user
COPY src /home/rif-user/src
EXPOSE 8080
COPY --from=build /home/rif-user/config.json /home/rif-user/
COPY --from=build /home/rif-user/target/rif-notifier-0.1.0.jar /home/rif-user/rif-notifier-0.1.0.jar
COPY --from=build /home/rif-user/bin /home/rif-user/
WORKDIR /home/rif-user
USER rif-user
RUN mkdir /home/rif-user/rif-notifier
ENTRYPOINT ["./rundocker.sh"]