package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class UndefinedStepDefinitionMatchTest {
    public final UndefinedPickleStepDefinitionMatch match = new UndefinedPickleStepDefinitionMatch(mock(PickleStep.class));

    @Test(expected=UndefinedStepDefinitionException.class)
    public void throws_ambiguous_step_definitions_exception_when_run() {
        match.runStep(mock(Scenario.class));
        fail("UndefinedStepDefinitionsException expected");
    }

    @Test(expected=UndefinedStepDefinitionException.class)
    public void throws_ambiguous_step_definitions_exception_when_dry_run() {
        match.dryRunStep(mock(Scenario.class));
        fail("UndefinedStepDefinitionsException expected");
    }
}
