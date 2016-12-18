package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FailedStepInstantiationMatchTest {
    public final static String ENGLISH = "en";
    private FailedStepInstantiationMatch match;

    @Before
    public void create_match() {
        PickleLocation location = mock(PickleLocation.class);
        when(location.getLine()).thenReturn(1);
        PickleStep step = mock(PickleStep.class);
        when(step.getLocations()).thenReturn(asList(location));
        when(step.getText()).thenReturn("step text");
        Exception exception = mock(Exception.class);
        StackTraceElement[] stackTrace = {new StackTraceElement("declaringClass", "methodName", "fileName", 1)};
        when(exception.getStackTrace()).thenReturn(stackTrace);
        match = new FailedStepInstantiationMatch("uri", step, exception);
    }

    @Test(expected=Exception.class)
    public void throws_the_exception_passed_to_the_match_when_run() throws Throwable {
        match.runStep(ENGLISH, mock(Scenario.class));
        fail("The exception passed to the FailedStepInstatiationMetch should be thrown");
    }

    @Test(expected=Exception.class)
    public void throws_the_exception_passed_to_the_match_when_dry_run() throws Throwable {
        match.dryRunStep(ENGLISH, mock(Scenario.class));
        fail("The exception passed to the FailedStepInstatiationMetch should be thrown");
    }
}
