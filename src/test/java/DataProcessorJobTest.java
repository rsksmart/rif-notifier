import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.RawData;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.scheduled.DataProcessorJob;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataProcessorJobTest {
    @Mock
    private DbManagerFacade dbManagerFacade;
    @InjectMocks
    @Autowired
    private DataProcessorJob dataProcessorJob;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canProcessRawData() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        List<Notification> ntfsData = new ArrayList<>();
        List<RawData> lstRawData = mockTestData.mockRawData();
        Subscription subscription = mockTestData.mockSubscription();
        lstSubs.add(subscription);
        Date date = new Date();
        lstRawData.forEach(rawDataItem -> {

            ntfsData.add(new Notification(subscription, new Timestamp(date.getTime()).toString(), false, rawDataItem.getData(), rawDataItem.getIdTopic()));
        });


        doReturn(lstRawData).when(dbManagerFacade).getRawDataByProcessed(false);
        doReturn(lstSubs).when(dbManagerFacade).getActiveSubscriptionsByTopicIdWithBalance(0);
        doReturn(lstSubs).when(dbManagerFacade).getActiveSubscriptionsByTopicIdWithBalance(1);

        dataProcessorJob.run();

        verify(dbManagerFacade, times(1)).saveNotificationBatch(any());
        verify(dbManagerFacade, times(1)).updateSubscription(any());
        verify(dbManagerFacade, times(1)).updateRawDataBatch(any());
    }
    @Test
    public void noDataToProcess() {
        List<RawData> lstRawData = new ArrayList<>();

        doReturn(lstRawData).when(dbManagerFacade).getRawDataByProcessed(false);

        dataProcessorJob.run();

        verify(dbManagerFacade, times(0)).saveNotificationBatch(any());
        verify(dbManagerFacade, times(0)).updateSubscription(any());
        verify(dbManagerFacade, times(0)).updateRawDataBatch(any());
    }
    @Test
    public void errorProcessRawDataSubsWithNoBalance() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        List<Notification> ntfsData = new ArrayList<>();
        List<RawData> lstRawData = mockTestData.mockRawData();
        Subscription subscription = mockTestData.mockSubscription();
        Date date = new Date();
        lstRawData.forEach(rawDataItem -> {
            ntfsData.add(new Notification(subscription, new Timestamp(date.getTime()).toString(), false, rawDataItem.getData(), rawDataItem.getIdTopic()));
        });

        doReturn(lstRawData).when(dbManagerFacade).getRawDataByProcessed(false);
        doReturn(lstSubs).when(dbManagerFacade).getActiveSubscriptionsByTopicIdWithBalance(0);
        doReturn(lstSubs).when(dbManagerFacade).getActiveSubscriptionsByTopicIdWithBalance(1);

        dataProcessorJob.run();

        verify(dbManagerFacade, times(0)).saveNotificationBatch(any());
        verify(dbManagerFacade, times(0)).updateSubscription(any());
        verify(dbManagerFacade, times(1)).updateRawDataBatch(any());
    }
    @Test
    public void errorProcessRawDataWithNoSubscription() {
        List<Subscription> lstSubs = new ArrayList<>();
        List<RawData> lstRawData = mockTestData.mockRawData();

        doReturn(lstRawData).when(dbManagerFacade).getRawDataByProcessed(false);
        doReturn(lstSubs).when(dbManagerFacade).getActiveSubscriptionsByTopicIdWithBalance(0);
        doReturn(lstSubs).when(dbManagerFacade).getActiveSubscriptionsByTopicIdWithBalance(1);

        dataProcessorJob.run();

        verify(dbManagerFacade, times(0)).saveNotificationBatch(any());
        verify(dbManagerFacade, times(0)).updateSubscription(any());
        verify(dbManagerFacade, times(1)).updateRawDataBatch(any());
    }
}
