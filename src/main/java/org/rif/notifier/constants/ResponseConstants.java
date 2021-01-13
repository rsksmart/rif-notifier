package org.rif.notifier.constants;

public interface ResponseConstants {

    String OK = "OK";

    String MISSING_APIKEY = "Apikey must be provided, first register the user to the notifier service";

    String INCORRECT_APIKEY = "Apikey not found, first register the user to the notifier service";

    String APIKEY_ALREADY_ADDED = "The user is already registered";

    String ADDRESS_NOT_PROVIDED = "Address is a required param, please insert a correct address";

    String SIGNED_ADDRESS_NOT_PROVIDED = "You need to provide your Address signed in the body of the request";

    String INCORRECT_SIGNED_ADDRESS = "The signed address provided is wrong";

    String SUBSCRIPTION_NOT_FOUND = "Subscription not found, first try to subscribe";

    String NO_ACTIVE_SUBSCRIPTION = "No active subscription found, check if you are subscribed first, or have a invoice pending of payment";

    String SUBSCRIPTION_ALREADY_ACTIVE = "This subscription is already active";

    String SUBSCRIPTION_ALREADY_ADDED = "This address is already subscribed";

    String SUBSCRIPTION_INCORRECT_TYPE = "The type you select is not a valid one";

    String TOPIC_VALIDATION_FAILED = "Topic structure failed, please review your json";

    String INVALID_TOPIC_ID = "The given topic id doesn't exist";

    String UNSUBSCRIBED_FROM_TOPIC_FAILED = "There was an error unsubscribing from the topic, or you're not subscribed to this topic";

    String AlREADY_SUBSCRIBED_TO_TOPIC = "This user is already subscribed to this topic";

    String AlREADY_SUBSCRIBED_TO_SOME_TOPICS = "This user is already subscribed to some topics of this list";

    String SUBSCRIPTION_OUT_OF_BALANCE = "Your subscription ran out of notification balance, please refill your subscription";

    String INCORRECT_TOKEN = "The token provided doesn't exist";

    String PREFERENCE_VALIDATION_FAILED = "Notification preference json has errors. Please review your json";

    String INVALID_EMAIL_ADDRESS = "Invalid email address found in destination field of json. Please ensure valid email address";

    String INVALID_PHONE_NUMBER = "Invalid phone number found in destination field of json. Please ensure valid phone number in format for ex. +13475555555";

    String SAVE_NOTIFICATION_PREFERENCE_FAILED = "There was an error while saving notification preference.";

    String PREFERENCE_NOT_FOUND = "Notification Preference not found for the given subscription, topic and type combination";

    String PREFERENCE_REMOVED = "The requested notification preference has been removed successfully";

    String DESTINATION_PARAMS_REQUIRED = "Destination params is required for api service";

    String SERVICE_NOT_ENABLED = "Notification Service is not enabled";

    String SUBSCRIPTION_PRICE_VALIDATION_FAILED = "Subscription Price structure is incorrect. Please review the json format";

    String SUBSCRIPTION_PLAN_ID_INVALID = "Invalid subscription plan id. Please provide a valid subscription plan id.";
}
