package integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.datatypes.Address;

import java.util.Optional;

import static integration.IntegrationTestData.TEST_ADDRESS;
import static org.junit.Assert.*;

/**
 * Integration test for CurrencyServices and CurrencyRepository
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, IntegrationTestData.class})
@ActiveProfiles("test")
public class CurrencyServiceIntegrationTest {

    @Autowired DbManagerFacade dbManagerFacade;
    @Autowired
    CurrencyRepository currencyRepository;


    @Autowired IntegrationTestData integrationTestData;

    Currency result;

    @Before
    public void setUp() {
        Currency c = new Currency("Test", TEST_ADDRESS);
        result = dbManagerFacade.saveCurrency(c);
    }

    @Test
    public void canSaveCurrency()   {
        assertEquals(TEST_ADDRESS, result.getAddress());
    }

    @Test(expected = NumberFormatException.class)
    public void errorInvalidCurrency()   {
        Currency c = new Currency("Test", new Address("integration-test-invalid-address"));
        Currency result = dbManagerFacade.saveCurrency(c);
        assertNull(result);
    }

    @Test
    public void canGetValidCurrencyByAddress()   {
        Optional<Currency> c = dbManagerFacade.getCurrencyByAddress(TEST_ADDRESS);
        assertTrue(c.isPresent());
        assertEquals(TEST_ADDRESS, c.get().getAddress());
    }

    @Test
    public void canGetValidCurrencyByName()   {
        Optional<Currency> c = dbManagerFacade.getCurrencyByName("Test");
        assertTrue(c.isPresent());
        assertEquals(TEST_ADDRESS, c.get().getAddress());
    }

    @Test
    public void errorGetCurrencyByName()   {
        Optional<Currency> c = dbManagerFacade.getCurrencyByName("Test-Invalid");
        assertFalse(c.isPresent());
    }

    @Test
    public void errorGetCurrencyByAddress()   {
        Optional<Currency> c = dbManagerFacade.getCurrencyByAddress(new Address("0xFFF"));
        assertFalse(c.isPresent());
    }

    @After
    public void tearDown()   {
        currencyRepository.delete(dbManagerFacade.getCurrencyByName("Test").get());
    }

}
