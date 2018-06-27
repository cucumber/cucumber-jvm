package cucumber.runtime;

import cucumber.api.Scenario;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.stepexpression.Argument;

import java.util.Collections;

public class AmbiguousPickleStepDefinitionsMatch extends PickleStepDefinitionMatch {
    private AmbiguousStepDefinitionsException exception;

    public AmbiguousPickleStepDefinitionsMatch(String uri, PickleStep step, AmbiguousStepDefinitionsException e) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), uri, step);
        this.exception = e;
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        throw exception;
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        runStep(language, scenario);
    }

    @Override
    public Match getMatch() {
        return exception.getMatches().get(0);
    }
}
