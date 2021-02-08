import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.controllers.SubscriptionPlanController;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.services.SubscriptionPlanServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubscriptionPlanController.class)
@ContextConfiguration(classes={Application.class})
public class SubscriptionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionPlanServices subscriptionPlanServices;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canGetSubscriptionPlans() throws Exception {

        List<SubscriptionPlan> plans = Arrays.asList(mockTestData.mockSubscriptionPlan());
        when(subscriptionPlanServices.getSubscriptionPlans()).thenReturn(plans);
        MvcResult result = mockMvc.perform(
                get("/getSubscriptionPlans")
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.writeValueAsString(plans), mapper.writeValueAsString(dtResponse.getContent()));
    }

    @Test
    public void canGetActiveSubscriptionPlans() throws Exception {

        SubscriptionPlan plan = mockTestData.mockSubscriptionPlan();
        SubscriptionPlan plan2 = mockTestData.mockSubscriptionPlan();
        //set the status to active
        plan.setStatus(true);
        List<SubscriptionPlan> plans = Arrays.asList(plan, plan2);
        when(subscriptionPlanServices.getSubscriptionPlans()).thenReturn(plans);
        MvcResult result = mockMvc.perform(
                get("/getSubscriptionPlans")
                .param("activePlansOnly", "true")
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        ObjectMapper mapper = new ObjectMapper();
        //ensure only active plan is returned
        assertEquals(mapper.writeValueAsString(Arrays.asList(plan)), mapper.writeValueAsString(dtResponse.getContent()));
    }

    @Test
    public void errorSubscriptionPlans() throws Exception {

        List<SubscriptionPlan> plans = Arrays.asList(mockTestData.mockSubscriptionPlan());
        when(subscriptionPlanServices.getSubscriptionPlans()).thenThrow(new RuntimeException("Internal Server Error"));
        MvcResult result = mockMvc.perform(
                get("/getSubscriptionPlans")
        )
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    @Test
    public void canGetSubscriptionPlan() throws Exception {

        SubscriptionPlan plan = mockTestData.mockSubscriptionPlan();
        when(subscriptionPlanServices.getSubscriptionPlan(anyInt())).thenReturn(plan);
        MvcResult result = mockMvc.perform(
                get("/getSubscriptionPlan/1")
        )
                .andExpect(status().isOk())
                .andReturn();
        DTOResponse dtResponse = new ObjectMapper().readValue(
                result.getResponse().getContentAsByteArray(),
                DTOResponse.class);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.writeValueAsString(plan), mapper.writeValueAsString(dtResponse.getContent()));
    }

    @Test
    public void errorGetSubscriptionPlan() throws Exception {

        SubscriptionPlan plan = mockTestData.mockSubscriptionPlan();
        MvcResult result = mockMvc.perform(
                get("/getSubscriptionPlan/0")
        )
                .andExpect(status().isConflict())
                .andReturn();
    }
}
