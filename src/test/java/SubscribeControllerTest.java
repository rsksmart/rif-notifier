import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.controllers.SubscribeController;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.util.Utils;
import org.rif.notifier.validation.SubscribeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubscribeController.class)
@ContextConfiguration(classes={Application.class, NotifierConfig.class})
public class SubscribeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    @MockBean
    private SubscribeServices subscribeServices;

    @MockBean
    private SubscribeValidator subscribeValidator;

    @MockBean
    private LuminoEventServices luminoEventServices;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canSubscribe() throws Exception {
        String address = "0x0";
        String luminoInvoice = "123457A90123457B901234C579012345D79012E345790F12345G790123H45790I";
        DTOResponse dto = new DTOResponse();
        dto.setContent(luminoInvoice);
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getActiveSubscriptionByAddress(us.getAddress())).thenReturn(null);
        when(subscribeServices.getSubscriptionPlanById(0)).thenReturn(subType);
        SubscriptionPrice price = mockTestData.mockSubscriptionPrice();
        MvcResult result = mockMvc.perform(
                post("/subscribe")
                        .contentType(APPLICATION_JSON_UTF8)
                        .param("planId", "0")
                        .header("apiKey", apiKey)
                        .content(price.toString())
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        assertEquals(dto.getStatus(), dtResponse.getStatus());
    }
    @Test
    public void canSendTopic() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = mockTestData.mockTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(subscribeServices.getActiveSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        //Need to mock with any, cause it was always returning false, maybe cause the Topic that we bring in here was not the same as in the controller
        when(subscribeServices.validateTopic(any(Topic.class))).thenReturn(true);
        when(subscribeValidator.validateTopic(any(Topic.class))).thenReturn(true);
        //when(subscribeServices.validateTopic(tp)).thenCallRealMethod();
        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
                        .param("planId","0")
                        .content(tp.toString())
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getStatus(), dtResponse.getStatus());
    }

    @Test
    public void canUnsubscribeFromTopic() throws Exception {
        int idTopic = 35;
        String apiKey = Utils.generateNewToken();
        User us = mockTestData.mockUser();
        Subscription sub = mockTestData.mockSubscription();
        Topic tp = mockTestData.mockTopic();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getTopicById(idTopic)).thenReturn(tp);
        when(subscribeServices.unsubscribeFromTopic(sub, tp)).thenReturn(true);

        MvcResult result = mockMvc.perform(
                post("/unsubscribeFromTopic")
                        .header("apiKey", apiKey)
                        .param("idTopic", String.valueOf(idTopic))
                        .param("planId", "0")
        )
                .andExpect(status().isOk())
                .andReturn();

        verify(subscribeServices, times(1)).unsubscribeFromTopic(sub, tp);
    }
    @Test
    public void errorUnsubscribeFromTopicWhenTopicDoesntExists() throws Exception {
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.INVALID_TOPIC_ID);
        int idTopic = 35;
        String apiKey = Utils.generateNewToken();
        User us = mockTestData.mockUser();
        Subscription sub = mockTestData.mockSubscription();
        Topic tp = mockTestData.mockTopic();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        when(subscribeServices.getTopicById(idTopic)).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/unsubscribeFromTopic")
                        .header("apiKey", apiKey)
                        .param("planId", "0")
                        .param("idTopic", String.valueOf(idTopic))
        )
                .andExpect(status().isConflict())
                .andReturn();

        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }

    @Test
    public void errorSubscribeToTopicWhenNotProvidingCorrectApiKey() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.INCORRECT_APIKEY);
        String apiKey = Utils.generateNewToken();
        Topic tp = mockTestData.mockTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(null);
        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
                        .param("planId", "0")
                        .content(tp.toString())
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
    @Test
    public void errorSubscribeToTopicWhenNotSubscribed() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        Topic tp = mockTestData.mockTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
                        .param("planId","0")
                        .content(tp.toString())
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
    @Test
    public void errorSubscribeToTopicWhenTopicWrong() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.TOPIC_VALIDATION_FAILED);
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = mockTestData.mockInvalidTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(subscribeServices.getActiveSubscriptionByAddressAndPlan(us.getAddress(), subType)).thenReturn(sub);

        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
                        .param("planId", "0")
                        .content(tp.toString())
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
    @Test
    public void errorSubscribeIncorrectApiKey() throws Exception {
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.INCORRECT_APIKEY);
        String apiKey = Utils.generateNewToken();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(null);

        SubscriptionPrice price = mockTestData.mockSubscriptionPrice();
        MvcResult result = mockMvc.perform(
                post("/subscribe")
                        .contentType(APPLICATION_JSON_UTF8)
                        .param("planId", "0")
                        .header("apiKey", apiKey)
                        .content(price.toString())
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
    /*@Test
    public void errorSubscribeIncorrectType() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.SUBSCRIPTION_INCORRECT_TYPE);
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionTypeByType(0)).thenReturn(null);
        MvcResult result = mockMvc.perform(
                post("/subscribe")
                        .param("type", "0")
                        .header("apiKey", apiKey)
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
     */
    @Test
    public void errorSubscribeNotProvidingApiKey() throws Exception {
        mockMvc.perform(
                post("/subscribe")
                        .param("planId", "0")
        )
                .andExpect(status().isBadRequest());
    }
    /*
    @Test
    public void errorSubscribeNotProvidingType() throws Exception {
        String apiKey = Utils.generateNewToken();

        mockMvc.perform(
                post("/subscribe")
                        .header("apiKey", apiKey)
        )
                .andExpect(status().isBadRequest());
    }
    */
}
