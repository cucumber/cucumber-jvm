package cucumber.runtime.java.weld;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;
import org.jboss.weld.environment.se.Weld;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeldFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        PrintStream originalErr = System.err;
        try {
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));
            ObjectFactory factory = new WeldFactory();
            factory.stop();
            assertThat(errContent.toString(), startsWith(WeldFactory.STOP_EXCEPTION_MESSAGE));
        } finally {
            System.setErr(originalErr);
        }
    }
}
