package cucumber.runtime;

import cucumber.api.Scenario;
import io.cucumber.messages.Messages.PickleStep;
import org.junit.Before;
import org.junit.Test;

import static cucumber.runtime.PickleHelper.step;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FailedStepInstantiationMatchTest {
    public final static String ENGLISH = "en";
    private FailedPickleStepInstantiationMatch match;

    @Before
    public void create_match() {
        PickleStep step = step();
        Exception exception = new Exception("oops");
        match = new FailedPickleStepInstantiationMatch("uri", step, exception);
    }

    @Test(expected = Exception.class)
    public void throws_the_exception_passed_to_the_match_when_run() throws Throwable {
        match.runStep(ENGLISH, mock(Scenario.class));
        fail("The exception passed to the FailedStepInstatiationMetch should be thrown");
    }

    @Test(expected = Exception.class)
    public void throws_the_exception_passed_to_the_match_when_dry_run() throws Throwable {
        match.dryRunStep(ENGLISH, mock(Scenario.class));
        fail("The exception passed to the FailedStepInstatiationMetch should be thrown");
    }
}
