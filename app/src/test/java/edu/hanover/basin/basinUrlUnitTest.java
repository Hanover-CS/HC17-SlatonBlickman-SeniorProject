package edu.hanover.basin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import edu.hanover.basin.Request.Objects.basinURL;

import static org.junit.Assert.*;

/**
 *  Unit tests for basinURL class, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class basinUrlUnitTest {

    private static final String basinWeb = "http://vault.hanover.edu/~blickmans15/services/basinWeb/v1/index.php";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void UserUrl_isCorrect(){
        basinURL url = new basinURL();
        assertEquals(url.toString(), basinWeb);

        assertEquals(url.getBasinWebURL(), basinWeb);

        url.getUserURL("");
        assertEquals(url.toString(), basinWeb + "/users/");

        url.getUserURL("0");
        assertEquals(url.toString(), basinWeb + "/users/0");

    }

    @Test
    public void EventUrl_isCorrect(){
        basinURL url = new basinURL();

        url.getEventURL("");
        assertEquals(url.toString(), basinWeb + "/events/");

        url.getEventURL("13");
        assertEquals(url.toString(), basinWeb + "/events/13");
    }

    @Test
    public void EventAttendeesUrl_isCorrect(){
        basinURL url = new basinURL();

        url.getEventAttendeesURL("1");
        assertEquals(url.toString(), basinWeb + "/events/1/attendees");

        url.getIsAttendingURL("1", "2");
        assertEquals(url.toString(), basinWeb  + "/events/1/attendees/2");
    }

    @Test
    public void IllegalArgument_isCorrect(){
        basinURL url = new basinURL();

        expectedException.expect(IllegalArgumentException.class);

        url.getEventAttendeesURL("");

        url.getIsAttendingURL("", "");

        url.getUserEventsURL("", null);

        url.getUserURL("", "");
    }

}
