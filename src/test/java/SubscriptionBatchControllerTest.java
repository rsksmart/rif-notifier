import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.rif.notifier.Application;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.controllers.SubscriptionBatchController;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.DTO.*;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.CurrencyServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.SubscriptionPlanServices;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.validation.CurrencyValidator;
import org.rif.notifier.validation.NotificationPreferenceValidator;
import org.rif.notifier.validation.SubscribeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.web3j.abi.datatypes.Address;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubscriptionBatchController.class)
@ContextConfiguration(classes={Application.class, NotifierConfig.class})
public class SubscriptionBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    @MockBean
    private SubscribeServices subscribeServices;

    @MockBean
    private NotificationPreferenceManager notificationPreferenceManager;

    @MockBean
    private NotificationPreferenceValidator notificationPreferenceValidator;

    @MockBean
    private SubscribeValidator subscribeValidator;

    @MockBean
    private CurrencyServices currencyServices;

    @MockBean
    private CurrencyValidator currencyValidator;

    @MockBean
    private SubscriptionPlanServices subscriptionPlanServices;

    private MockTestData mockTestData = new MockTestData();
    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        String address = "0x0";
        User us = new User(address, null);
        Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        List<TopicDTO> topics = mockTestData.mockTopics();
        when(userServices.userExists(anyString())).thenReturn(us);
        when(subscriptionPlanServices.getActiveSubscriptionPlan(anyInt())).thenReturn(Optional.of(subType));
        when(subscribeServices.getSubscriptionByHash(anyString())).thenReturn(subscription);
        when(subscribeServices.createSubscription(any(User.class), any(SubscriptionPlan.class), any(SubscriptionPrice.class))).thenReturn("");
        when(subscribeServices.getActiveSubscriptionByAddressAndPlan(anyString(), any(SubscriptionPlan.class))).thenReturn(null);
        when(subscribeValidator.validateTopic(any(Topic.class))).thenReturn(true);
        when(subscribeServices.createPendingSubscription(any(User.class), any(SubscriptionPlan.class), any(SubscriptionPrice.class))).thenReturn(subscription);
        when(subscribeServices.subscribeAndGetTopic(any(Topic.class), any(Subscription.class))).thenReturn(mockTestData.mockTopic());
        when(notificationPreferenceManager.saveNotificationPreference(any(NotificationPreference.class))).thenReturn(null);
    }

    private SubscriptionBatchDTO prepareBatchTest() throws Exception{
        return prepareBatchTest(mockTestData.mockSubscription());
    }

    private SubscriptionBatchDTO prepareBatchTest(Subscription prev)  throws Exception {
        SubscriptionBatchDTO subscriptionBatch = mockTestData.mockSubscriptionBatch();
        prev.setHash("test");
        SubscriptionDTO subscriptionDTO = mockTestData.mockSubscriptionDTO();
        SubscriptionBatchResponse response = new SubscriptionBatchResponse("test", "testsignature", subscriptionDTO);
        when(subscribeServices.getSubscriptionByHash(anyString())).thenReturn(prev);
        when(subscribeServices.getSubscriptionByHashAndUserAddress(anyString(),anyString())).thenReturn(prev);
        when(subscribeServices.createSubscriptionDTO(any(Subscription.class), any(List.class), any(User.class))).thenReturn(subscriptionDTO);
        when(subscribeServices.getSubscriptionHash(any(SubscriptionDTO.class))).thenReturn("testhash");
        when(subscribeServices.createSubscriptionBatchResponse(any(SubscriptionDTO.class), anyString())).thenReturn(response);
        when(currencyServices.getCurrencyByName(anyString())).thenReturn(mockTestData.mockCurrency());
        return subscriptionBatch;
    }

    @Test
    public void canSubscribeBatch() throws Exception {
        prepareBatchTest();
        SubscriptionBatchDTO subscriptionBatch = mockTestData.mockSubscriptionBatch();
        DTOResponse dto = new DTOResponse();
        MvcResult result = mockMvc.perform(
                post("/subscribeToPlan")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(mapper.writeValueAsString(subscriptionBatch))
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        assertEquals(dtResponse.getStatus(), dto.getStatus());
    }

    @Test
    public void errorInvalidTopic() throws Exception {
        SubscriptionBatchDTO subscriptionBatch = mockTestData.mockSubscriptionBatch();
        DTOResponse dto = new DTOResponse();
        when(subscribeValidator.validateTopic(any(Topic.class))).thenReturn(false);
        MvcResult result = mockMvc.perform(
                post("/subscribeToPlan")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(mapper.writeValueAsString(subscriptionBatch))
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        assertEquals(dtResponse.getStatus(), HttpStatus.CONFLICT);
    }

    @Test
    public void errorInvalidNotificationPreference() throws Exception {
        SubscriptionBatchDTO subscriptionBatch = mockTestData.mockSubscriptionBatch();
        DTOResponse dto = new DTOResponse();
        doThrow(ValidationException.class).when(notificationPreferenceValidator).validate(anyList(), any(SubscriptionPlan.class));
        MvcResult result = mockMvc.perform(
                post("/subscribeToPlan")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(mapper.writeValueAsString(subscriptionBatch))
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        assertEquals(dtResponse.getStatus(), HttpStatus.CONFLICT);
    }

    @Test
    public void canGetSignedResponse()  throws Exception    {
        MvcResult result = mockMvc.perform(
                post("/subscribeToPlan")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(mapper.writeValueAsString(prepareBatchTest()))
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        Map content = (Map)dtResponse.getContent();
        assertEquals("testsignature", content.get("signature"));
        assertEquals("test", content.get("hash"));
    }

    @Test
    public void canRenewBatch() throws Exception {
        DTOResponse dto = new DTOResponse();
        Subscription prev = mockTestData.mockSubscription();
        SubscriptionBatchDTO batchDTO = prepareBatchTest(prev);
        Subscription newSubscription = mockTestData.mockSubscription();
        assertNotEquals(prev, newSubscription.getPreviousSubscription());
        when(subscribeServices.createPendingSubscription(any(User.class), any(SubscriptionPlan.class), any(SubscriptionPrice.class))).thenReturn(newSubscription);
        MvcResult result = mockMvc.perform(
                post("/renewSubscription")
                        .contentType(APPLICATION_JSON_UTF8)
                        .param("previousSubscriptionHash", "test")
                        .content(mapper.writeValueAsString(batchDTO))
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        Map content = (Map)dtResponse.getContent();
        assertEquals("test", content.get("hash"));
        assertEquals(prev, newSubscription.getPreviousSubscription());
    }

    @Test
    public void errorRenewBatchMissingRequestParam()   throws Exception    {
        DTOResponse dto = new DTOResponse();
        MvcResult result = mockMvc.perform(
                post("/renewSubscription")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(mapper.writeValueAsString(prepareBatchTest()))
        )
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void errorRenewBatchHashNotFound() throws Exception {
        DTOResponse dto = new DTOResponse();
        SubscriptionBatchDTO batchDTO = prepareBatchTest();
        when(subscribeServices.getSubscriptionByHashAndUserAddress(anyString(),anyString())).thenReturn(null);
        MvcResult result = mockMvc.perform(
                post("/renewSubscription")
                        .contentType(APPLICATION_JSON_UTF8)
                        .param("previousSubscriptionHash", "invalidhash")
                        .content(mapper.writeValueAsString(batchDTO))
        )
                .andExpect(status().isConflict())
                .andReturn();
    }

    private void canGetSubscriptions(boolean auth) throws Exception {
        Subscription sub = mockTestData.mockSubscription();
        Subscription sub2 = mockTestData.mockSubscription();
        sub.setNotificationPreferences(Arrays.asList(mockTestData.mockNotificationPreference(sub)));
        sub2.setNotificationPreferences(Arrays.asList(mockTestData.mockNotificationPreference(sub)));
        SubscriptionDTO dto = new SubscriptionDTO();
        User user = auth? new User() : null;
        sub.setActiveSince(null);
        sub2.setActiveSince(null);
        //set the status to active
        List<Subscription> subscriptions = Arrays.asList(sub, sub2);
        doCallRealMethod().when(subscribeServices).createSubscriptionDTOs(anyList(), nullable(User.class));
        doCallRealMethod().when(subscribeServices).createTopicDTO(any(Subscription.class), nullable(User.class));
        doCallRealMethod().when(subscribeServices).createSubscriptionDTO(any(Subscription.class), anyList(), nullable(User.class));
        List<SubscriptionDTO> subscriptionDTOs = subscribeServices.createSubscriptionDTOs(subscriptions, user);
        when(subscribeServices.getSubscriptionByAddress(anyString())).thenReturn(subscriptions);
        if (auth) {
            when(userServices.authenticate(anyString(), nullable(String.class))).thenReturn(user);
        }
        MvcResult result = mockMvc.perform(
                get("/getSubscriptions")
                .header("userAddress", "0x0")
                .header("apiKey", "test")
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        ObjectMapper mapper = new ObjectMapper();
        //ensure only active plan is returned
        assertEquals(mapper.writeValueAsString(subscriptionDTOs), mapper.writeValueAsString(dtResponse.getContent()));
        //verify all notification preference details returned if auth is true
        if(auth)    {
            assertEquals(ArrayList.class, subscriptionDTOs.get(0).getTopics().get(0).getNotificationPreferences().getClass());
        }
        //verify only name of the notification service is returned if no auth user
        else    {
            assertEquals(String.class, subscriptionDTOs.get(0).getTopics().get(0).getNotificationPreferences().getClass());
        }
    }

    @Test
    public void canGetSubscriptionsNoAuth() throws Exception {
        canGetSubscriptions(false);
    }
    @Test
    public void canGetSubscriptionsAuth() throws Exception {
        canGetSubscriptions(true);
    }

    @Test
    public void failGetSubscriptionsNoUserAddress() throws Exception {

        Subscription sub = mockTestData.mockSubscription();
        Subscription sub2 = mockTestData.mockSubscription();
        //set the status to active
        List<Subscription> subscriptions = Arrays.asList(sub, sub2);
        when(subscribeServices.getSubscriptionByAddress(anyString())).thenReturn(subscriptions);
        MvcResult result = mockMvc.perform(
                get("/getSubscriptions")
        )
                .andExpect(status().is4xxClientError())
                .andReturn();
    }
}
