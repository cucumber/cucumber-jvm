package cucumber.runtime;

import cucumber.api.Argument;
import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

public class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    public UndefinedPickleStepDefinitionMatch(PickleStep step) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), null, step, null);
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        throw new UndefinedStepDefinitionException();
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        runStep(language, scenario);
    }

}
