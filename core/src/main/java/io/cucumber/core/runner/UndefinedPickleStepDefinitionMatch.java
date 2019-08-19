package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.feature.CucumberStep;

import java.util.Collections;

final class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    UndefinedPickleStepDefinitionMatch(String uri, CucumberStep step) {
        super(Collections.emptyList(), new NoStepDefinition(), uri, step);
    }

    @Override
    public void runStep(Scenario scenario) {
        throw new UndefinedStepDefinitionException();
    }

    @Override
    public void dryRunStep(Scenario scenario) {
        runStep(scenario);
    }

}
