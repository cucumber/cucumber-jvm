package cucumber.runner;

import io.cucumber.stepexpression.Argument;
import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    UndefinedPickleStepDefinitionMatch(PickleStep step) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), null, step);
    }

    @Override
    public void runStep(Scenario scenario) {
        throw new UndefinedStepDefinitionException();
    }

    @Override
    public void dryRunStep(Scenario scenario) throws Throwable {
        runStep(scenario);
    }

}
