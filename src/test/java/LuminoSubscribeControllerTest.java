import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.controllers.LuminoSubscribeController;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LuminoSubscribeController.class)
@ContextConfiguration(classes={Application.class})
public class LuminoSubscribeControllerTest {

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
    public void canSubscribeToOpenChannel() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = mockTestData.mockTopicOpenChannelWithoutFilters();
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getActiveSubscriptionByHashAndUserAddress(anyString(),anyString())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelOpenedTopicForToken("12345", null, null)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
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
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = mockTestData.mockTopicOpenChannelWithoutFilters();
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getActiveSubscriptionByHashAndUserAddress(anyString(),anyString())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelOpenedTopicForToken("12345", participantOne, participantTwo)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("token", "12345")
                        .param("subscriptionHash", "0")
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
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = luminoEventServices.getChannelClosedTopicForToken(token, null, null);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getActiveSubscriptionByHashAndUserAddress(anyString(), anyString())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelClosedTopicForToken("12345", null, null)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToCloseChannel")
                        .header("userAddress", address)
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
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
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = luminoEventServices.getChannelClosedTopicForToken(token, channelIdentifier, closeParticipant);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getActiveSubscriptionByHashAndUserAddress(anyString(), anyString())).thenReturn(sub);
        when(luminoEventServices.isToken(any())).thenReturn(true);
        when(luminoEventServices.getChannelClosedTopicForToken(token, channelIdentifier, closeParticipant)).thenReturn(tp);
        when(subscribeServices.getTopicByHashCodeAndIdSubscription(tp, sub.getId())).thenReturn(null);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("userAddress", closeParticipant)
                        .header("apiKey", apiKey)
                        .param("token", token)
                        .param("subscriptionHash", "0")
                        .param("closingparticipant", closeParticipant)
                        .param("channelidentifier", String.valueOf(channelIdentifier))
        )
                .andExpect(status().isOk())
                .andReturn();

        verify(subscribeServices, times(1)).subscribeToTopic(tp, sub);
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
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        Topic tp = mockTestData.mockTopicOpenChannelWithoutFilters();
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getActiveSubscriptionByHashAndUserAddress(anyString(), anyString())).thenReturn(sub);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        when(luminoEventServices.isToken(any())).thenReturn(false);
        when(luminoEventServices.getChannelOpenedTopicForToken("12345", null, null)).thenReturn(tp);

        MvcResult result = mockMvc.perform(
                post("/subscribeToOpenChannel")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
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
        SubscriptionPlan subType = new SubscriptionPlan(1000);
        Subscription sub = new Subscription(new Date(), us.getAddress(), subType, SubscriptionStatus.ACTIVE);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(),subType)).thenReturn(sub);
        when(subscribeServices.getActiveSubscriptionByHashAndUserAddress(anyString(), anyString())).thenReturn(sub);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(sub).collect(Collectors.toList()));
        when(luminoEventServices.isToken(any())).thenReturn(false);

        MvcResult result = mockMvc.perform(
                post("/subscribeToCloseChannel")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("token", token)
                        .param("subscriptionHash", "0")
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
