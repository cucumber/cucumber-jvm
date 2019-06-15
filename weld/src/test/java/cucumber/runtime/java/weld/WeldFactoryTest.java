package cucumber.runtime.java.weld;

import cucumber.runtime.CucumberException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import org.jboss.weld.environment.se.Weld;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeldFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LogRecordListener logRecordListener;

    @Before
    public void setup() {
        logRecordListener = new LogRecordListener();
        LoggerFactory.addListener(logRecordListener);
    }

    @After
    public void tearDown(){
        LoggerFactory.removeListener(logRecordListener);
    }

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {

        final ObjectFactory factory = new WeldFactory();
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.start();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    @Test
    public void startStopCalledWithoutStart() {

        final Weld weld = mock(Weld.class);
        when(weld.initialize())
            .thenThrow(new IllegalArgumentException());

        final WeldFactory factory = new WeldFactory();

        this.expectedException.expect(CucumberException.class);
        this.expectedException.expectMessage(is(equalTo(WeldFactory.START_EXCEPTION_MESSAGE)));

        factory.start(weld);
    }

    @Test
    public void stopCalledWithoutStart() {
        ObjectFactory factory = new WeldFactory();
        factory.stop();
        assertThat(logRecordListener.getLogRecords().get(0).getMessage(), containsString(WeldFactory.STOP_EXCEPTION_MESSAGE));
    }
}
