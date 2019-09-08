package io.cucumber.core.runner;

import io.cucumber.core.feature.CucumberStep;

import java.util.Collections;

final class FailedPickleStepInstantiationMatch extends PickleStepDefinitionMatch {
    private final Throwable throwable;

    FailedPickleStepInstantiationMatch(String uri, CucumberStep step, Throwable throwable) {
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
