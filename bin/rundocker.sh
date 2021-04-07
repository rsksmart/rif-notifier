#!/bin/bash
#script to run rif-notifier

#wait for mysql to start
sleep 60

json=$(</home/rif-user/config.json)

#run the application
java -Dspring.application.json="$json" -jar rif-notifier-0.1.0.jar
