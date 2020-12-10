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
import static org.mockito.Mockito.times;


@RunWith(MockitoJUnitRunner.class)
public class NotificationJobTest {
    @Mock
    private DbManagerFacade dbManagerFacade;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void mockTest()  {

    }


}

