#!/bin/bash
#script to run rif-notifier

#modify required properties in config.json
#change the port number to a different port from 8080 if there is a local port conflict

json=$(<config.json)

$M2_HOME/bin/mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.application.json='$json'"
