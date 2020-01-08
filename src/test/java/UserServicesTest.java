import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.UserServices;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class UserServicesTest {
    @InjectMocks
    private UserServices userServices;

    @Mock
    private DbManagerFacade dbManagerFacade;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void userExists(){
        // given
        User user = mockTestData.mockUser();

        doReturn(user).when(dbManagerFacade).getUserByAddress(user.getAddress());

        // when
        boolean retVal = userServices.userExists(user.getAddress()) != null;

        // then
        assertTrue(retVal);
    }
    @Test
    public void errorUserNotExists(){
        // given
        User user = mockTestData.mockUser();

        doReturn(null).when(dbManagerFacade).getUserByAddress(user.getAddress());

        // when
        boolean retVal = userServices.userExists(user.getAddress()) != null;

        // then
        assertFalse(retVal);
    }
    @Test
    public void getUserByApiKey(){
        // given
        String apiKey = "1234567891234";
        User user = mockTestData.mockUser();

        doReturn(user).when(dbManagerFacade).getUserByApiKey(apiKey);

        // when
        User retVal = userServices.getUserByApiKey(apiKey);

        // then
        assertEquals(user, retVal);
    }
}
