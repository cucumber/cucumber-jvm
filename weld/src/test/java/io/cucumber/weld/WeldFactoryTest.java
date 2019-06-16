package io.cucumber.weld;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

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
    public void stopCalledWithoutStart() {
        ObjectFactory factory = new WeldFactory();
        factory.stop();
        assertThat(logRecordListener.getLogRecords().get(0).getMessage(),
            containsString("your weld container didn't shut down properly"));
    }
}
