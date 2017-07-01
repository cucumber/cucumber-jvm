package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

public class AmbiguousStepDefinitionsMatch extends StepDefinitionMatch {
    private AmbiguousStepDefinitionsException exception;

    public AmbiguousStepDefinitionsMatch(PickleStep step, AmbiguousStepDefinitionsException e) {
        super(null, new NoStepDefinition(), null, step, null);
        this.exception = e;
    }

    @Override
    public Object runStep(String language, Scenario scenario) throws Throwable {
        throw exception;
    }

    @Override
    public Object dryRunStep(String language, Scenario scenario) throws Throwable {
        return runStep(language, scenario);
    }

    @Override
    public Match getMatch() {
        return exception.getMatches().get(0);
    }
}
