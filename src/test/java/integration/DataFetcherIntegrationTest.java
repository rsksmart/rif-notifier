package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.exception.RSKBlockChainException;
import org.rif.notifier.scheduled.DataFetchingJob;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, IntegrationTestData.class})
@ActiveProfiles("test")
public class DataFetcherIntegrationTest {
    @Autowired DataFetchingJob dataFetcingJob;
    @Autowired RskBlockchainService rskBlockchainService;

    @Test
    public void canGetLastConfirmedBlock()    throws Exception   {
        BigInteger lastBlock = rskBlockchainService.getLastBlock();
        BigInteger blockConfirmationCount = lastBlock.intValue() >= 20 ? BigInteger.valueOf(20) : BigInteger.ZERO;
        BigInteger lastConfirmedBlock = rskBlockchainService.getLastConfirmedBlock(blockConfirmationCount);
        assertEquals(lastBlock.subtract(blockConfirmationCount), lastConfirmedBlock);
    }

    @Test(expected=RSKBlockChainException.class)
    public void errorGetLastConfirmedBlock()    throws Exception   {
        //get hundred blocks ahead to cause rsk exception
        BigInteger blockConfirmationCount = BigInteger.valueOf(-100);
        BigInteger lastConfirmedBlock = rskBlockchainService.getLastConfirmedBlock(blockConfirmationCount);
    }
}
