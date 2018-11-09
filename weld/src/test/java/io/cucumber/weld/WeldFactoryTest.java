package io.cucumber.weld;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.backend.ObjectFactory;

import org.jboss.weld.environment.se.Weld;
import org.junit.After;
import org.junit.Before;
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

    private static final PrintStream ORIGINAL_OUT = System.out;
    private static final PrintStream ORIGINAL_ERR = System.err;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(ORIGINAL_OUT);
        System.setErr(ORIGINAL_ERR);
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
    public void startstopCalledWithoutStart() {

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

        final ObjectFactory factory = new WeldFactory();

        factory.stop();

        final String expectedErrOutput = "\n" +
            "If you have set enabled=false in org.jboss.weld.executor.properties and you are seeing\n" +
            "this message, it means your weld container didn't shut down properly. It's a Weld bug\n" +
            "and we can't do much to fix it in Cucumber-JVM.\n" +
            "\n" +
            "java.lang.NullPointerException\n" +
            "\tat io.cucumber.weld.WeldFactory.stop";

        assertThat(this.errContent.toString(), is(startsWith(expectedErrOutput)));
    }

}
