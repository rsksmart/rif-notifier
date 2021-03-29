#!/bin/bash
#script to create / update subscription plans

#modify subscription-plan.json under resources folder to specify your own subscription plan. This provided file is just an example. All the fields in json are required.

#Only enabled notification preferences in application.properties can be specified as part of notificationPreferences for each subscription plan in the json.

#modify required properties in config-docker.json
#change the port number to a different port from 8080 if there is a local port conflict

#wait for mysql to start
sleep 60

json=$(</home/rif-user/config.json)

#load subscription plan to db
java -Dspring.application.json="$json" -jar rif-notifier-0.1.0.jar loadSubscriptionPlan
#run the application
java -Dspring.application.json="$json" -jar rif-notifier-0.1.0.jar
