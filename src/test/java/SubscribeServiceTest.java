import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.blockchain.lumino.LuminoInvoice;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubscribeServiceTest {
    @InjectMocks
    private SubscribeServices subscribeServices;

    @Mock
    private DbManagerFacade dbManagerFacade;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canCreateSubscription() throws IOException {
        // given
        User user = mockTestData.mockUser();
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();
        Subscription sub = mockTestData.mockSubscription();
        String luminoVal = LuminoInvoice.generateInvoice(user.getAddress());

        //doReturn(type).when(dbManagerFacade).getSubscriptionTypeByType(0);

        // when
        String retVal = subscribeServices.createSubscription(user, type, null);

        // then
        assertEquals(luminoVal, retVal);
    }
    @Test
    public void canSubscribeToTopicNewTopic() throws IOException {
        // given
        Subscription subscription = mockTestData.mockSubscription();
        Topic topic = mockTestData.mockTopic();

        doReturn(topic).when(dbManagerFacade).saveTopic(topic.getType(), "" + topic.hashCode(), subscription);

        // when
        subscribeServices.subscribeToTopic(topic, subscription);

        // then
        for (TopicParams param : topic.getTopicParams()) {
            verify(dbManagerFacade, times(1)).saveTopicParams(
                    topic, param.getType(), param.getValue(), param.getOrder(), param.getValueType(), param.getIndexed(), param.getFilter()
            );
        }

        //verify(dbManagerFacade, times(1)).updateTopic(topic);
    }
    @Test
    public void canSubscribeToTopicUpdateTopic() throws IOException {
        // given
        Subscription subscription = mockTestData.mockSubscription();
        Topic topic = mockTestData.mockTopic();

        doReturn(topic).when(dbManagerFacade).getTopicByHashCode(topic.hashCode());

        // when
        subscribeServices.subscribeToTopic(topic, subscription);

        // then
        verify(dbManagerFacade, times(1)).updateTopic(topic);
    }
    @Test
    public void errorCreateSubscriptionNotProvidingUser(){
        // given
        User user = null;
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();

        // when
        String retVal = subscribeServices.createSubscription(user, type, null);

        // then
        assertEquals("", retVal);
    }
    @Test
    public void errorCreateSubscriptionNotProvidingType(){
        // given
        User user = mockTestData.mockUser();
        SubscriptionPlan type = null;

        // when
        String retVal = subscribeServices.createSubscription(user, type, null);

        // then
        assertEquals("", retVal);
    }
    /*
    @Test
    public void errorCreateSubscriptionInvalidType(){
        // given
        User user = mockTestData.mockUser();
        SubscriptionType type = mockTestData.mockSubscriptionType();

        doReturn(null).when(dbManagerFacade).getSubscriptionTypeByType(0);
        // when
        String retVal = subscribeServices.createSubscription(user, type);

        // then
        assertEquals("", retVal);
    }
     */
    @Test
    public void isSubscriptionTypeValid() {
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();

        doReturn(type).when(dbManagerFacade).getSubscriptionPlanById(0);

        boolean retVal = subscribeServices.isSubscriptionPlanValid(0);

        assertTrue(retVal);
    }
    @Test
    public void errorSubscriptionTypeInvalid() {
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();

        doReturn(null).when(dbManagerFacade).getSubscriptionPlanById(0);

        boolean retVal = subscribeServices.isSubscriptionPlanValid(0);

        assertFalse(retVal);
    }
    @Test
    public void getSubscriptionTypeByType() {
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();

        doReturn(type).when(dbManagerFacade).getSubscriptionPlanById(0);

        SubscriptionPlan retVal = subscribeServices.getSubscriptionPlanById(0);

        assertEquals(retVal, type);
    }
    @Test
    public void canValidateTopic() throws IOException {
        Topic topic = mockTestData.mockTopic();

        boolean retVal = subscribeServices.validateTopic(topic);

        assertTrue(retVal);
    }
    @Test
    public void errorValidateTopicInvalidParams() throws IOException {
        Topic topic = mockTestData.mockInvalidTopic();

        boolean retVal = subscribeServices.validateTopic(topic);

        assertFalse(retVal);
    }
    @Test
    public void errorValidateTopicInvalidTopicType() throws IOException {
        Topic topic = mockTestData.mockInvalidTopic();

        boolean retVal = subscribeServices.validateTopic(topic);

        assertFalse(retVal);
    }
    @Test
    public void getActiveSubscriptionByAddress() throws IOException {
        Subscription subscription = mockTestData.mockSubscription();

        doReturn(Stream.of(subscription).collect(Collectors.toList())).when(dbManagerFacade).getActiveSubscriptionByAddress("0x0");

        List<Subscription> retVal = subscribeServices.getActiveSubscriptionByAddress("0x0");

        assertTrue(retVal.stream().allMatch(s->s.isActive()));
    }
    @Test
    public void canActivateSubscription() throws IOException {
        // given
        Subscription activeSubscription = mockTestData.mockSubscription();
        Subscription inactiveSubscription = mockTestData.mockInactiveSubscription();

        doReturn(activeSubscription).when(dbManagerFacade).updateSubscription(inactiveSubscription);

        // when
        boolean retVal = subscribeServices.activateSubscription(inactiveSubscription);

        // then
        assertTrue(retVal);
    }
    @Test
    public void errorActivateSubscriptionAlreadyActive() throws IOException {
        // given
        Subscription activeSubscription = mockTestData.mockSubscription();
        Subscription inactiveSubscription = mockTestData.mockInactiveSubscription();

        // when
        boolean retVal = subscribeServices.activateSubscription(activeSubscription);

        // then
        assertFalse(retVal);
    }
    @Test
    public void canAddBalanceToSubscription() throws IOException {
        // given
        String luminoInvoice = "123457A90123457B901234C579012345D79012E345790F12345G790123H45790I";
        Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();

        doReturn(subscription).when(dbManagerFacade).updateSubscription(subscription);

        // when
        String retVal = subscribeServices.addBalanceToSubscription(subscription, type);

        // then
        assertEquals(luminoInvoice, retVal);
    }
    @Test
    public void errorAddBalanceToSubscriptionNotProvidingSubscription(){
        // given
        String expected = "";
        //Subscription subscription = mockTestData.mockSubscription();
        SubscriptionPlan type = mockTestData.mockSubscriptionPlan();

        //doReturn(subscription).when(dbManagerFacade).updateSubscription(subscription);

        // when
        String retVal = subscribeServices.addBalanceToSubscription(null, type);

        // then
        assertEquals(expected, retVal);
    }
    @Test
    public void errorAddBalanceToSubscriptionNotProvidingSubscriptionType() throws IOException {
        // given
        String expected = "";
        Subscription subscription = mockTestData.mockSubscription();
        //SubscriptionType type = mockTestData.mockSubscriptionType();

        //doReturn(subscription).when(dbManagerFacade).updateSubscription(subscription);

        // when
        String retVal = subscribeServices.addBalanceToSubscription(subscription, null);

        // then
        assertEquals(expected, retVal);
    }

    @Test
    public void canUnsubscribeFromTopic()   throws IOException  {
        Topic topic = mockTestData.mockTopic();
        Subscription sub = mockTestData.mockSubscription();
        Set<Subscription> subs = new HashSet<>();
        subs.add(sub);
        topic.setSubscriptions(subs);
        doReturn(topic).when(dbManagerFacade).updateTopic(topic);
        assertTrue(subscribeServices.unsubscribeFromTopic(sub, topic));
    }

    @Test
    public void errorUnsubscribeFromTopic()   throws IOException  {
        Topic topic = mockTestData.mockTopic();
        Subscription sub = mockTestData.mockSubscription();
        Set<Subscription> subs = new HashSet<>();
        subs.add(sub);
        topic.setSubscriptions(subs);
        doReturn(null).when(dbManagerFacade).updateTopic(topic);
        assertFalse(subscribeServices.unsubscribeFromTopic(sub, topic));
    }

    @Test
    public void errorUnsubscribeFromTopicSubscriptionMismatch()   throws IOException  {
        Topic topic = mockTestData.mockTopic();
        Subscription sub = mockTestData.mockSubscription();
        Subscription subOther = mockTestData.mockSubscription();
        subOther.setUserAddress("other");
        Set<Subscription> subs = new HashSet<>();
        subs.add(sub);
        topic.setSubscriptions(subs);
        assertFalse(subscribeServices.unsubscribeFromTopic(subOther, topic));
    }

    @Test
    public void errorSubscribeToTopic() {
        assertEquals(SubscriptionResponse.INVALID, subscribeServices.subscribeToTopic(null, null));
    }

    @Test
    public void canGetSubscriptionByAddress() throws IOException    {
        Subscription sub = mockTestData.mockSubscription();
        doReturn(Stream.of(sub).collect(Collectors.toList())).when(dbManagerFacade).getSubscriptionByAddress(sub.getUserAddress());
        assertEquals(sub, subscribeServices.getSubscriptionByAddress(sub.getUserAddress()).get(0));
    }

    @Test
    public void errorGetSubscriptionByAddress() throws IOException    {
        Subscription sub = mockTestData.mockSubscription();
        doReturn(null).when(dbManagerFacade).getSubscriptionByAddress(sub.getUserAddress());
        assertNull(subscribeServices.getSubscriptionByAddress(sub.getUserAddress()));
    }

    @Test
    public void canGetTopicByHashcodeAndIdSubscription()    throws IOException  {
        Topic topic = mockTestData.mockTopic();
        doReturn(topic).when(dbManagerFacade).getTopicByHashCodeAndIdSubscription(topic.hashCode(), 0);
        assertEquals(topic, subscribeServices.getTopicByHashCodeAndIdSubscription(topic, 0));
    }

    @Test
    public void errorGetTopicByHashcodeAndIdSubscription()    throws IOException  {
        Topic topic = mockTestData.mockTopic();
        doReturn(null).when(dbManagerFacade).getTopicByHashCodeAndIdSubscription(topic.hashCode(), 0);
        assertNull(subscribeServices.getTopicByHashCodeAndIdSubscription(topic, 0));
    }
}
