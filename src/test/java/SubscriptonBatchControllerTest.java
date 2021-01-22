import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.controllers.SubscriptionBatchController;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.DTO.SubscriptionBatchDTO;
import org.rif.notifier.models.DTO.SubscriptionDTO;
import org.rif.notifier.models.DTO.TopicDTO;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubscriptionBatchController.class)
@ContextConfiguration(classes={Application.class, NotifierConfig.class})
public class SubscriptonBatchControllerTest {

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
        when(subscribeServices.getSubscriptionPlanById(anyInt())).thenReturn(subType);
        when(subscribeServices.getSubscriptionByAddressAndPlan(anyString(), any(SubscriptionPlan.class))).thenReturn(subscription);
        when(subscribeServices.createSubscription(any(User.class), any(SubscriptionPlan.class), any(SubscriptionPrice.class))).thenReturn("");
        when(subscribeServices.getActiveSubscriptionByAddressAndPlan(anyString(), any(SubscriptionPlan.class))).thenReturn(null);
        when(subscribeValidator.validateTopic(any(Topic.class))).thenReturn(true);
        when(subscribeServices.createPendingSubscription(any(User.class), any(SubscriptionPlan.class), any(SubscriptionPrice.class))).thenReturn(subscription);
        when(subscribeServices.subscribeAndGetTopic(any(Topic.class), any(Subscription.class))).thenReturn(mockTestData.mockTopic());
        when(notificationPreferenceManager.saveNotificationPreference(any(NotificationPreference.class))).thenReturn(null);
    }

    @Test
    public void canSubscribeBatch() throws Exception {
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
        doThrow(ValidationException.class).when(notificationPreferenceValidator).validate(anyList());
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
        SubscriptionBatchDTO subscriptionBatch = mockTestData.mockSubscriptionBatch();
        SubscriptionDTO subscriptionDTO = mockTestData.mockSubscriptionDTO();
        Map resultMap = new HashMap();
        resultMap.put("subscription", subscriptionDTO);
        resultMap.put("hash", "test");
        resultMap.put("signature", "testsignature");
        DTOResponse dto = new DTOResponse();
        when(subscribeServices.createSubscriptionDTO(any(SubscriptionBatchDTO.class), any(Subscription.class), anyString())).thenReturn(subscriptionDTO);
        when(subscribeServices.getSubscriptionHash(any(SubscriptionDTO.class))).thenReturn("testhash");
        when(subscribeServices.buildSubscriptionResponseMap(any(SubscriptionDTO.class), anyString(), anyString())).thenReturn(resultMap);
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
        Map content = (Map)dtResponse.getContent();
        assertEquals("testsignature", content.get("signature"));
        assertEquals("test", content.get("hash"));
    }
}

