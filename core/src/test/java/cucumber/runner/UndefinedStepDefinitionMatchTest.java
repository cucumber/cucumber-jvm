package cucumber.runner;

import cucumber.api.Scenario;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class UndefinedStepDefinitionMatchTest {

    private PickleStep pickleStep = new PickleStep("text", Collections.<Argument>emptyList(), singletonList(new PickleLocation(5, 5)));
    public final UndefinedPickleStepDefinitionMatch match = new UndefinedPickleStepDefinitionMatch("path/to.feature", pickleStep);

    @Test(expected = UndefinedStepDefinitionException.class)
    public void throws_ambiguous_step_definitions_exception_when_run() {
        match.runStep(mock(Scenario.class));
        fail("UndefinedStepDefinitionsException expected");
    }

    @Test(expected = UndefinedStepDefinitionException.class)
    public void throws_ambiguous_step_definitions_exception_when_dry_run() {
        match.dryRunStep(mock(Scenario.class));
        fail("UndefinedStepDefinitionsException expected");
    }
}
