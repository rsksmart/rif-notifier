package org.rif.notifier.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.re2j.Pattern;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NotificationPreferenceValidator extends BaseValidator  {

    //multiple email addreses separated by comma
    private static final Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern phoneRegex = Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$");

    private NotifierConfig notifierConfig;
    private NotificationPreferenceManager notificationPreferenceManager;
    private SubscribeServices subscribeServices;

    public NotificationPreferenceValidator(@Autowired UserServices userServices, @Autowired NotifierConfig notifierConfig,
                                           @Autowired NotificationPreferenceManager notificationPreferenceManager,
                                           @Autowired SubscribeServices subscribeServices)    {
        super(userServices);
        this.notifierConfig = notifierConfig;
        this.notificationPreferenceManager = notificationPreferenceManager;
        this.subscribeServices = subscribeServices;
    }

    public void validate(List<NotificationPreference> preferences)  {
        //validate each notification preference for the topic
        if(preferences.size() > preferences.stream().distinct().count())    {
            throw new ValidationException("Duplicate notification preferences found, please correct your json.");
        }
        preferences.forEach(preference-> {
            validateRequestNotificationPreference(preference);
        });
    }

    public void validateRequestNotificationPreference(NotificationPreference preference)   throws ValidationException {
        boolean enabled = notifierConfig.getEnabledServices().stream().anyMatch(p->preference.getNotificationService() == p);
        if (!enabled)   throw new ValidationException(ResponseConstants.SERVICE_NOT_ENABLED);
        if (preference.getNotificationService() == NotificationServiceType.EMAIL) {
            validateEmail(preference);
        }
        else if (preference.getNotificationService() == NotificationServiceType.SMS) {
            validateSMS(preference);
        }
        else if (preference.getNotificationService() == NotificationServiceType.API) {
            validateAPI(preference);
        }
    }

    private void validateSMS(NotificationPreference preference) throws ValidationException {
        //validate phone with international prefix +13475555555
        if (!phoneRegex.matcher(preference.getDestination()).matches()) {
            throw new ValidationException(ResponseConstants.INVALID_PHONE_NUMBER);
        }
    }

    private void validateEmail(NotificationPreference preference) {
        //validate email in case of email service type
        List<String> emails = Arrays.asList(preference.getDestination().split(";"));
        emails.forEach(email->{
            if(!p.matcher(email).matches())   {
                throw new ValidationException(ResponseConstants.INVALID_EMAIL_ADDRESS);
            }
        });
    }

    private void validateAPI(NotificationPreference preference) {
        //validate api destination url
        try {
            URL url = new URL(preference.getDestination());
        } catch(MalformedURLException e)    {
            throw new ValidationException(ResponseConstants.INVALID_DESTINATION_URL);
        }        //validate that api has destination params
        Optional.ofNullable(preference.getDestinationParams())
                .orElseThrow(()->new ValidationException(ResponseConstants.DESTINATION_PARAMS_REQUIRED));
    }

    public NotificationPreference validateRequestJson(String notificationPreference)  {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(notificationPreference, NotificationPreference.class);
        }catch(IOException e)   {
            throw new ValidationException(ResponseConstants.PREFERENCE_VALIDATION_FAILED, e);
        }
    }

    /*
     * check if notification preference already associated with same topic from another subscription for given user, if no topic specified, default topic 0 will be used
     * get all subscriptions by user address and verify if there is already a subscription with the given notification preference
     * for the given topic
     */
    public void validateNoExistingUserSubcriptionForTopicAndPreference(Subscription subscription, NotificationPreference requestedPreference)    {
        List<Subscription> subs = subscribeServices.getSubscriptionByAddress(subscription.getUserAddress());
        subs = subs.stream().filter(s->!s.equals(subscription)).collect(Collectors.toList());
        subs.forEach(sub-> {
            Optional.ofNullable(notificationPreferenceManager.getNotificationPreference(sub, requestedPreference.getIdTopic(), requestedPreference.getNotificationService())).ifPresent(p-> {
                throw new SubscriptionException("The notification service is already asssociated with another subscription for the same topic for this user ");
            });
        });
    }
}
