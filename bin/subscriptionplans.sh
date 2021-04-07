#!/bin/bash
#script to create / update subscription plans

#Only enabled notification preferences in application.properties can be specified as part of notificationPreferences for each subscription plan in the json.

#change the port number to a different port from 8080 if there is a local port conflict

serverport=8180
json=$(<config.json)

$M2_HOME/bin/mvn clean spring-boot:run -Dspring-boot.run.arguments="loadSubscriptionPlan" -Dspring-boot.run.jvmArguments="-Dserverport=$serverport -Dspring.application.json='$json'"
