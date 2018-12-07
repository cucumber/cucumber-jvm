package cucumber.runner;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.Argument;

import java.util.Collections;

class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    UndefinedPickleStepDefinitionMatch(String uri, PickleStep step) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), uri, step);
    }

    @Override
    public void runStep(Scenario scenario) {
        throw createUndefinedStepDefinitionException();
    }

    private UndefinedStepDefinitionException createUndefinedStepDefinitionException() {
        UndefinedStepDefinitionException undefined = new UndefinedStepDefinitionException(getStep().getText());
        undefined.setStackTrace(new StackTraceElement[]{getStepLocation()});
        return undefined;
    }

    @Override
    public void dryRunStep(Scenario scenario) {
        runStep(scenario);
    }

}
