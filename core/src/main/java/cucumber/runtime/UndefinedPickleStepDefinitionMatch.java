package cucumber.runtime;

import cucumber.api.Scenario;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.stepexpression.Argument;

import java.util.Collections;

public class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    public UndefinedPickleStepDefinitionMatch(PickleStep step) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), null, step);
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
