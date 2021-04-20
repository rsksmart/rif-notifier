#!/bin/bash
#script to create / update subscription plans

#Only enabled notification preferences in application.properties can be specified as part of notificationPreferences for each subscription plan in the json.

json=$(</home/rif-user/config.json)
json2=$(echo $json | sed -e "s/8080/8180/")
#load subscription plan to db
java -Dspring.application.json="$json2" -jar rif-notifier-0.1.0.jar $*
