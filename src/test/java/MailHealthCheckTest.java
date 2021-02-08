import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.health.MailHealthIndicator;
import org.rif.notifier.health.SMSHealthIndicator;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.DTO.SubscriptionDTO;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.blockchain.lumino.LuminoInvoice;
import org.rif.notifier.util.Utils;
import org.rif.notifier.validation.SubscribeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MailHealthCheckTest {
    @InjectMocks @Autowired
    private MailHealthIndicator mailHealth;
    @Mock private JavaMailSenderImpl mailSender;
    @Mock private NotifierConfig config;

    public MailHealthCheckTest() {
    }

    @Test
    public void errorMailService()   throws Exception {
        doThrow(RuntimeException.class).when(mailSender).testConnection();
        when(config.getEnabledServices()).thenReturn(Arrays.asList(NotificationServiceType.EMAIL));
        Health health = mailHealth.health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    public void canCheckMailServiceUp()   throws Exception {
        when(config.getEnabledServices()).thenReturn(Arrays.asList(NotificationServiceType.EMAIL));
        Health health = mailHealth.health();
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    public void canCheckMailServiceDisabled()   throws Exception {
        when(config.getEnabledServices()).thenReturn(Collections.emptyList());
        Health health = mailHealth.health();
        assertEquals("disabled", health.getStatus().getCode());
    }

}

