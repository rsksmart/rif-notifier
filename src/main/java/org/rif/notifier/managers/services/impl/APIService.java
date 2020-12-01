package org.rif.notifier.managers.services.impl;

import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

/**
 * Service for sending notification to an API Destination
 */
@Service
public class APIService implements NotificationService {
    private static final String API_KEY_HEADER = "apiKey";
    private static final Logger logger = LoggerFactory.getLogger(APIService.class);

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
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(API_KEY_HEADER, notification.getNotificationPreference().getDestinationParams().getApiKey());
        HttpEntity<String> request = new HttpEntity<>(notification.getNotification().getData(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(destination, request, String.class);
        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new NotificationException(response.getBody(), null);
        }
        return response.getBody();
    }
}
