package cucumber.runtime;

import io.cucumber.stepexpression.Argument;
import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

public class FailedPickleStepInstantiationMatch extends PickleStepDefinitionMatch {
    private final Throwable throwable;

    public FailedPickleStepInstantiationMatch(String uri, PickleStep step, Throwable throwable) {
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

}
