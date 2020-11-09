import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.controllers.SubscribeController;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionType;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubscribeController.class)
@ContextConfiguration(classes={Application.class})
public class SubscribeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    @MockBean
    private SubscribeServices subscribeServices;

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
        SubscriptionType subType = new SubscriptionType(1000);
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getActiveSubscriptionByAddress(us.getAddress())).thenReturn(null);
        when(subscribeServices.getSubscriptionTypeByType(0)).thenReturn(subType);
        MvcResult result = mockMvc.perform(
                post("/subscribe")
                        .param("type", "0")
                        .header("apiKey", apiKey)
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
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = mockTestData.mockTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        //Need to mock with any, cause it was always returning false, maybe cause the Topic that we bring in here was not the same as in the controller
        when(subscribeServices.validateTopic(any(Topic.class))).thenReturn(true);
        //when(subscribeServices.validateTopic(tp)).thenCallRealMethod();
        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
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
    public void canSubscribeToOpenChannel() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = mockTestData.mockTopicOpenChannelWithoutFilters();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelOpenedTopicForToken("12345", null, null)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("apiKey", apiKey)
                        .param("token", "12345")
        )
                .andExpect(status().isOk())
                .andReturn();

        verify(subscribeServices, times(1)).subscribeToTopic(tp, sub);
    }
    @Test
    public void canSubscribeToOpenChannelWithFilters() throws Exception {
        String participantOne = "0x0";
        String participantTwo = "0x1";
        String apiKey = Utils.generateNewToken();
        User us = new User(participantOne, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = mockTestData.mockTopicOpenChannelWithoutFilters();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelOpenedTopicForToken("12345", participantOne, participantTwo)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("apiKey", apiKey)
                        .param("token", "12345")
                        .param("participantone", participantOne)
                        .param("participanttwo", participantTwo)
        )
                .andExpect(status().isOk())
                .andReturn();

        verify(subscribeServices, times(1)).subscribeToTopic(tp, sub);
    }
    @Test
    public void canSubscribeToCloseChannel() throws Exception {
        String address = "0x0";
        String token = "12345";
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = luminoEventServices.getChannelClosedTopicForToken(token, null, null);
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelClosedTopicForToken("12345", null, null)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToCloseChannel")
                        .header("apiKey", apiKey)
                        .param("token", token)
        )
                .andExpect(status().isOk())
                .andReturn();

        verify(subscribeServices, times(1)).subscribeToTopic(tp, sub);
    }
    @Test
    public void canSubscribeToCloseChannelWithFilters() throws Exception {
        String closeParticipant = "0x0";
        String token = "12345";
        Integer channelIdentifier = 1;
        String apiKey = Utils.generateNewToken();
        User us = new User(closeParticipant, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = luminoEventServices.getChannelClosedTopicForToken(token, channelIdentifier, closeParticipant);
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelClosedTopicForToken(token, channelIdentifier, closeParticipant)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("apiKey", apiKey)
                        .param("token", token)
                        .param("closingparticipant", closeParticipant)
                        .param("channelidentifier", String.valueOf(channelIdentifier))
        )
                .andExpect(status().isOk())
                .andReturn();

        verify(subscribeServices, times(1)).subscribeToTopic(tp, sub);
    }
    @Test
    public void canUnsubscribeFromTopic() throws Exception {
        int idTopic = 35;
        String apiKey = Utils.generateNewToken();
        User us = mockTestData.mockUser();
        Subscription sub = mockTestData.mockSubscription();
        Topic tp = mockTestData.mockTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(subscribeServices.getTopicById(idTopic)).thenReturn(tp);
        when(subscribeServices.unsubscribeFromTopic(sub, tp)).thenReturn(true);

        MvcResult result = mockMvc.perform(
                post("/unsubscribeFromTopic")
                        .header("apiKey", apiKey)
                        .param("idTopic", String.valueOf(idTopic))
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
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(subscribeServices.getTopicById(idTopic)).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/unsubscribeFromTopic")
                        .header("apiKey", apiKey)
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
    public void errorSubscribeToOpenChannelNoTokenProvided() throws Exception {
        String apiKey = Utils.generateNewToken();

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("apiKey", apiKey)
        )
                .andExpect(status().isBadRequest())
                .andReturn();
    }
    @Test
    public void errorSubscribeToOpenChannelIncorrectTokenProvided() throws Exception {
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.INCORRECT_TOKEN);
        String participantOne = "0x0";
        String apiKey = Utils.generateNewToken();
        User us = new User(participantOne, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = mockTestData.mockTopicOpenChannelWithoutFilters();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(false);
        when(luminoEventServices.getChannelOpenedTopicForToken("12345", null, null)).thenReturn(tp);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("apiKey", apiKey)
                        .param("token", "54321")
        )
                .andExpect(status().isConflict())
                .andReturn();

        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
    @Test
    public void errorSubscribeToCloseChannelNoTokenProvided() throws Exception {
        String apiKey = Utils.generateNewToken();

        mockMvc.perform(
                post("/subscribeToCloseChannel")
                        .header("apiKey", apiKey)
        )
                .andExpect(status().isBadRequest());
    }
    @Test
    public void errorSubscribeToCloseChannelIncorrectTokenProvided() throws Exception {
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.INCORRECT_TOKEN);
        String closeParticipant = "0x0";
        String token = "12345";
        Integer channelIdentifier = 1;
        String apiKey = Utils.generateNewToken();
        User us = new User(closeParticipant, apiKey);
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(false);

        MvcResult result = mockMvc.perform(
                post("/subscribeToCloseChannel")
                        .header("apiKey", apiKey)
                        .param("token", token)
                        .param("closingparticipant", closeParticipant)
                        .param("channelidentifier", String.valueOf(channelIdentifier))
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
        Topic tp = mockTestData.mockTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
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
        SubscriptionType subType = new SubscriptionType(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, "PAYED");
        Topic tp = mockTestData.mockInvalidTopic();
        when(userServices.getUserByApiKey(apiKey)).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(sub);

        MvcResult result = mockMvc.perform(
                post("/subscribeToTopic")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("apiKey", apiKey)
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
                        .param("type", "0")
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
