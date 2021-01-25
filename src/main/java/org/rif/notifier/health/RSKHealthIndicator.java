package org.rif.notifier.health;

import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Verifies if rsk blockchain is up
 */
@Component
public class RSKHealthIndicator extends AbstractHealthIndicator {

    private RskBlockchainService rskBlockchainService;

    public RSKHealthIndicator(@Autowired RskBlockchainService rskBlockchainService)    {
        super("RSK Blockchain health check failed");
        this.rskBlockchainService = rskBlockchainService;
    }

    /**
     * Checks and updates the status of rsk block chain
     * configuration.
     * @param builder
     * @throws Exception when rsk blockchain is down. The exception will result in
     * status being down.
     */
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception    {
        rskBlockchainService.getLastBlock();
        builder.up();
    }
}
