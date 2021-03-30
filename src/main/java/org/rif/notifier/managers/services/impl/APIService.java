package org.rif.notifier.managers.services.impl;

import com.sun.jndi.toolkit.url.Uri;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.helpers.EncryptHelper;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

/**
 * Service for sending notification to an API Destination
 */
@Service
public class APIService implements NotificationService {
    private static final String API_KEY_HEADER = "apiKey";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final Logger logger = LoggerFactory.getLogger(APIService.class);

    @Autowired private EncryptHelper encryptHelper;

    /**
     * A post request with apiKey parameter in header is sent.
     * If response is HttpStatus.OK then response is sent, otherwise the error
     * @param notification
     * @return
     * @throws NotificationException
     */
    @Override
    public String sendNotification(NotificationLog notification) throws NotificationException {
        RestTemplate restTemplate = new RestTemplate();
        logger.info("sending api notification for id " + notification.getNotification().getId());
        String destination = notification.getNotificationPreference().getDestination();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<String> username = Optional.ofNullable(notification.getNotificationPreference().getDestinationParams().getUsername());
        Optional<String> password = Optional.ofNullable(notification.getNotificationPreference().getDestinationParams().getPassword());
        Optional<String> apiKey = Optional.ofNullable(notification.getNotificationPreference().getDestinationParams().getApiKey());

        username.ifPresent(uname -> headers.add(USERNAME, uname));
        boolean https = false;
        try {
            URL url = new URL(destination);
            https = "https".equals(url.getProtocol());
        } catch(MalformedURLException e)    {
            logger.error(e.getMessage(), e);
        }
        //send auth data only thru https
        if(https) {
            password.ifPresent(pass -> headers.add(PASSWORD, encryptHelper.decrypt(pass)));
            apiKey.ifPresent(key -> headers.add(API_KEY_HEADER, encryptHelper.decrypt(key)));
        }
        HttpEntity<String> request = new HttpEntity<>(notification.getNotification().getData(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(destination, request, String.class);
        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new NotificationException(response.getBody(), null);
        }
        //result_text in notification_log is tinytext, so needs to be truncated
        String result = response.getBody().substring(0, Math.min(response.getBody().length(), 255));
        return result;
    }
}
