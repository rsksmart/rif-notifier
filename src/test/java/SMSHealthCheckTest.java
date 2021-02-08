import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.health.SMSHealthIndicator;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SMSHealthCheckTest {
    @InjectMocks @Autowired
    private SMSHealthIndicator SMSHealth;
    @Mock private NotifierConfig config;
    @Mock
    TwilioRestClient twilioRestClient;

    public SMSHealthCheckTest() {
    }

    @Test
    public void errorSMSService()   {
        when(config.getEnabledServices()).thenReturn(Arrays.asList(NotificationServiceType.SMS));
        Health health = SMSHealth.health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    public void canCheckSMSServiceDisabled()   throws Exception {
        when(config.getEnabledServices()).thenReturn(Collections.emptyList());
        Health health = SMSHealth.health();
        assertEquals("disabled", health.getStatus().getCode());
    }

}

