#script to create / update subscription plans

#modify subscription-plan.json under resources folder to specify your own subscription plan. This provided file is just an example. All the fields in json are required.

#Only enabled notification preferences in application.properties can be specified as part of notificationPreferences for each subscription plan in the json.

#modify required properties in config.json
#change the port number to a different port from 8080 if there is a local port conflict

json=$(<config.json)

$M2_HOME/bin/mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.application.json='$json'"
