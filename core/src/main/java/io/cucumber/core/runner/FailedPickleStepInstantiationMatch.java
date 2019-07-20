package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import io.cucumber.core.api.Scenario;

import java.util.Collections;

final class FailedPickleStepInstantiationMatch extends PickleStepDefinitionMatch {
    private final Throwable throwable;

    FailedPickleStepInstantiationMatch(String uri, PickleStep step, Throwable throwable) {
        super(Collections.emptyList(), new NoStepDefinition(), uri, step);
        this.throwable = removeFrameworkFramesAndAppendStepLocation(throwable, getStepLocation());
    }

    @Override
    public void runStep(Scenario scenario) throws Throwable {
        throw throwable;
    }
    public void dryRunStep(Scenario scenario) throws Throwable {
        runStep(scenario);
    }

}
