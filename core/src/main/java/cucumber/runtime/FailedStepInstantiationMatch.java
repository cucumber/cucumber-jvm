package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

public class FailedStepInstantiationMatch extends StepDefinitionMatch {
    private final Throwable throwable;

    public FailedStepInstantiationMatch(PickleStep step, Throwable throwable) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), step.getLocations().get(0).getPath(), step, null);
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
