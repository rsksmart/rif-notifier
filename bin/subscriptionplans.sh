#modify subscription-plan.json under resources folder to specify your own subscription plan. This provided file is just an example. All the fields in json are required.
#Only enabled notification preferences in application.properties can be specified as part of notificationPreferences for each subscription plan in the json.


$M2_HOME/bin/mvn spring-boot:run -Dspring-boot.run.arguments=loadSubscriptionPlan
