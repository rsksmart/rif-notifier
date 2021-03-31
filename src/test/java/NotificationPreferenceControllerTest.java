import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.controllers.NotificationPreferenceController;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.helpers.EncryptHelper;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.util.Utils;
import org.rif.notifier.validation.NotificationPreferenceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = NotificationPreferenceController.class)
@ContextConfiguration(classes={Application.class})
public class NotificationPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    @MockBean
    private SubscribeServices subscribeServices;

    @MockBean
    private NotificationPreferenceManager notificationPreferenceManager;

    @MockBean
    private NotifierConfig notifierConfig;

    @MockBean
    private NotificationPreferenceValidator validator;

    @MockBean
    private EncryptHelper encryptHelper;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canSaveNotificationPreference() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        NotificationPreference pref = mockTestData.mockNotificationPreference(subscription);
        dto.setContent(pref);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(), subType)).thenReturn(subscription);
        when(subscribeServices.getActiveSubscriptionByHash(anyString())).thenReturn(subscription);
        when(validator.validateRequestJson(any(String.class))).thenReturn(pref);
        //save notification
        when(notificationPreferenceManager.saveNotificationPreference(any(NotificationPreference.class))).thenReturn(pref);
        doReturn(Arrays.asList(NotificationServiceType.SMS, NotificationServiceType.EMAIL, NotificationServiceType.API)).when(notifierConfig).getEnabledServices();
        ObjectMapper mapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(
                post("/saveNotificationPreference")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
                        .content(mapper.writeValueAsString(pref))
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getStatus(), dtResponse.getStatus());
    }

    @Test
    public void canRemoveNotificationPreference() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        NotificationPreference pref = mockTestData.mockNotificationPreference(subscription);
        dto.setContent(pref);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(), subType)).thenReturn(subscription);
        when(subscribeServices.getActiveSubscriptionByHash(anyString())).thenReturn(subscription);
        when(validator.validateRequestJson(any(String.class))).thenReturn(pref);
        //save notification
        when(notificationPreferenceManager.getNotificationPreference(any(Subscription.class), any(Integer.class), any(NotificationServiceType.class))).thenReturn(pref);
        ObjectMapper mapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(
                post("/removeNotificationPreference")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
                        .content(mapper.writeValueAsString(pref))
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getStatus(), dtResponse.getStatus());
    }

    @Test
    public void errorInvalidEmailAddress() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        NotificationPreference pref = mockTestData.mockNotificationPreference(subscription);
        pref.setDestination("test.com");
        dto.setContent(pref);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(), subType)).thenReturn(subscription);
        when(subscribeServices.getActiveSubscriptionByHash(anyString())).thenReturn(subscription);
        when(validator.validateRequestJson(any(String.class))).thenReturn(pref);
        doThrow(ValidationException.class).when(validator).validateRequestNotificationPreference(pref);
        //save notification
        when(notificationPreferenceManager.saveNotificationPreference(any(NotificationPreference.class))).thenReturn(pref);
        ObjectMapper mapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(
                post("/saveNotificationPreference")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
                        .content(mapper.writeValueAsString(pref))
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(HttpStatus.CONFLICT, dtResponse.getStatus());
    }

    @Test
    public void errorInvalidNotificationService() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan subType = mockTestData.mockSubscriptionPlan();
        NotificationPreference pref = mockTestData.mockNotificationPreference(subscription);
        dto.setContent(pref);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionPlanById(subType.getId())).thenReturn(subType);
        //when(subscribeServices.getSubscriptionByAddressAndPlan(us.getAddress(), subType)).thenReturn(subscription);
        when(subscribeServices.getActiveSubscriptionByHash(anyString())).thenReturn(subscription);
        when(validator.validateRequestJson(any(String.class))).thenReturn(pref);
        doThrow(ValidationException.class).when(validator).validateRequestNotificationPreference(pref);
        //when(validator.validateRequestNotificationPreference(any(NotificationPreference.class))).thenTh
        //save notification
        when(notificationPreferenceManager.saveNotificationPreference(any(NotificationPreference.class))).thenReturn(pref);
        ObjectMapper mapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(
                post("/saveNotificationPreference")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
                        .param("subscriptionHash", "0")
                        .content(mapper.writeValueAsString(pref).replace("EMAIL", "INVALID"))
        )
                .andExpect(status().isConflict())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(HttpStatus.CONFLICT, dtResponse.getStatus());
    }
}

