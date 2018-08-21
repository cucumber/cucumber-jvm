package cucumber.runner;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class AmbiguousStepDefinitionMatchsTest {
    public static final String ENGLISH = "en";
    public final AmbiguousStepDefinitionsException e = mock(AmbiguousStepDefinitionsException.class);
    public final AmbiguousPickleStepDefinitionsMatch match = new AmbiguousPickleStepDefinitionsMatch("uri", mock(PickleStep.class), e);

    @Test
    public void throws_ambiguous_step_definitions_exception_when_run() {
        try {
            match.runStep(ENGLISH, mock(Scenario.class));
            fail("AmbiguousStepDefinitionsException expected");
        } catch (Throwable thrown) {
            assertEquals(e, thrown);
        }
    }

    @Test
    public void throws_ambiguous_step_definitions_exception_when_dry_run() {
        try {
            match.dryRunStep(ENGLISH, mock(Scenario.class));
            fail("AmbiguousStepDefinitionsException expected");
        } catch (Throwable thrown) {
            assertEquals(e, thrown);
        }
    }
}
