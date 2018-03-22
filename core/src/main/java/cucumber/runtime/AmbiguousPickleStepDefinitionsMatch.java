package cucumber.runtime;

import cucumber.api.Argument;
import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

public class AmbiguousPickleStepDefinitionsMatch extends PickleStepDefinitionMatch {
    private AmbiguousStepDefinitionsException exception;

    public AmbiguousPickleStepDefinitionsMatch(String uri, PickleStep step, AmbiguousStepDefinitionsException e) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), uri, step, null);
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

}
