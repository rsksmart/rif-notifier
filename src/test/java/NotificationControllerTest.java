import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.controllers.NotificationController;
import org.rif.notifier.services.NotificationServices;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.entities.User;
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

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = NotificationController.class)
@ContextConfiguration(classes={Application.class})
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    @MockBean
    private SubscribeServices subscribeServices;

    @MockBean
    private NotificationServices notificationServices;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canBringNotifications() throws Exception {
        String address = "0x0";
        DTOResponse dto = new DTOResponse();
        String apiKey = Utils.generateNewToken();
        User us = new User(address, apiKey);
        Subscription subscription = mockTestData.mockSubscription();
        List<Notification> notifs = mockTestData.mockNotifications();
        dto.setContent(notifs);
        when(userServices.authenticate(anyString(), anyString())).thenReturn(us);
        when(subscribeServices.getSubscriptionByAddress(us.getAddress())).thenReturn(Stream.of(subscription).collect(Collectors.toList()));
        //Return notifications
        when(notificationServices.getNotificationsForSubscription(subscription, null, null, null)).thenReturn(notifs);
        mockMvc.perform(
                get("/getNotifications")
                        .header("userAddress", us.getAddress())
                        .header("apiKey", apiKey)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.*", hasSize(10)));
    }

    @Test
    public void errorWhenNotProvidingCorrectApiKey() throws Exception {
        DTOResponse dto = new DTOResponse();
        dto.setMessage(ResponseConstants.INCORRECT_APIKEY);
        String apiKey = Utils.generateNewToken();
        Topic tp = mockTestData.mockTopic();
        when(userServices.authenticate(anyString(), anyString())).thenThrow(new CredentialException(ResponseConstants.INCORRECT_APIKEY));
        MvcResult result = mockMvc.perform(
                get("/getNotifications")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("userAddress", "0x0")
                        .header("apiKey", apiKey)
                        .content(tp.toString())
        )
                .andExpect(status().isUnauthorized())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);

        assertEquals(dto.getMessage(), dtResponse.getMessage());
    }
}
