import mocked.MockTestData;
import org.hibernate.HibernateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.DTO.SubscriptionDTO;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscriptionPlanServices;
import org.rif.notifier.services.blockchain.lumino.LuminoInvoice;
import org.rif.notifier.util.Utils;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionPlanServicesTest {
    @InjectMocks
    private SubscriptionPlanServices subscribeServices;

    @Mock
    private DbManagerFacade dbManagerFacade;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canGetSubscriptionPlans()   {
        List<SubscriptionPlan> plans = Arrays.asList(mockTestData.mockSubscriptionPlan());
        when(dbManagerFacade.getSubscriptionPlans()).thenReturn(plans);
        List<SubscriptionPlan> result = subscribeServices.getSubscriptionPlans();
        assertEquals(plans, result);
    }

    @Test(expected=HibernateException.class)
    public void errorGetSubscriptionPlans()   {
        List<SubscriptionPlan> plans = Arrays.asList(mockTestData.mockSubscriptionPlan());
        when(dbManagerFacade.getSubscriptionPlans()).thenThrow(HibernateException.class);
        subscribeServices.getSubscriptionPlans();
    }

    @Test
    public void canGetSubscriptionPlan()   {
        SubscriptionPlan plan = mockTestData.mockSubscriptionPlan();
        when(dbManagerFacade.getSubscriptionPlanById(anyInt())).thenReturn(plan);
        SubscriptionPlan result = subscribeServices.getSubscriptionPlan(0);
        assertEquals(plan, result);
    }
}

