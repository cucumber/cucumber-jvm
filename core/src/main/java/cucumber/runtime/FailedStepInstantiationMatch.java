package cucumber.runtime;

import cucumber.stepexpression.Argument;
import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

public class FailedStepInstantiationMatch extends StepDefinitionMatch {
    private final Throwable throwable;

    public FailedStepInstantiationMatch(String uri, PickleStep step, Throwable throwable) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), uri, step);
        this.throwable = removeFrameworkFramesAndAppendStepLocation(throwable, getStepLocation());
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        throw throwable;
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        runStep(language, scenario);
    }

    @Override
    public Match getMatch() {
        return Match.UNDEFINED;
    }
}
