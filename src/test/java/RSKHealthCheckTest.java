import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.exception.RSKBlockChainException;
import org.rif.notifier.health.RSKHealthIndicator;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RSKHealthCheckTest {
    @InjectMocks @Autowired
    private RSKHealthIndicator rskHealth;
    @Mock private RskBlockchainService rskBlockChainService;

    public RSKHealthCheckTest() {
    }

    @Test
    public void errorRskService()   throws Exception {
        when(rskBlockChainService.getLastBlock()).thenThrow(RuntimeException.class);
        Health health = rskHealth.health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    public void canCheckRSKServiceUp()   throws Exception {
        Health health = rskHealth.health();
        assertEquals(Status.UP, health.getStatus());
    }


}

