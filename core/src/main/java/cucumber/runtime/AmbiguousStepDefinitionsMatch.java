package cucumber.runtime;

import cucumber.api.Argument;
import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

public class AmbiguousStepDefinitionsMatch extends StepDefinitionMatch {
    private AmbiguousStepDefinitionsException exception;

    public AmbiguousStepDefinitionsMatch(String uri, PickleStep step, AmbiguousStepDefinitionsException e) {
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
