#script to create / update subscription plans

#modify subscription-plan.json under resources folder to specify your own subscription plan. This provided file is just an example. All the fields in json are required.
#Only enabled notification preferences in application.properties can be specified as part of notificationPreferences for each subscription plan in the json.

#change the port number to a different port from 8180 if there is a local port conflict

$M2_HOME/bin/mvn clean spring-boot:run -Dspring-boot.run.arguments=loadSubscriptionPlan -Dspring-boot.run.jvmArguments="-Dserver.port=8180"
