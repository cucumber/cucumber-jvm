package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class UndefinedStepDefinitionMatchTest {
    public final static String ENGLISH = "en";
    public final UndefinedStepDefinitionMatch match = new UndefinedStepDefinitionMatch(mock(PickleStep.class));

    @Test(expected=UndefinedStepDefinitionException.class)
    public void throws_ambiguous_step_definitions_exception_when_run() throws Throwable {
        match.runStep(ENGLISH, mock(Scenario.class));
        fail("UndefinedStepDefinitionsException expected");
    }

    @Test(expected=UndefinedStepDefinitionException.class)
    public void throws_ambiguous_step_definitions_exception_when_dry_run() throws Throwable {
        match.dryRunStep(ENGLISH, mock(Scenario.class));
        fail("UndefinedStepDefinitionsException expected");
    }
}
