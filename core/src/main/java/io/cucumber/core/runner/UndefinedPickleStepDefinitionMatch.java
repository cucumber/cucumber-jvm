package io.cucumber.core.runner;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.feature.CucumberStep;

import java.net.URI;
import java.util.Collections;

final class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    UndefinedPickleStepDefinitionMatch(URI uri, CucumberStep step) {
        super(Collections.emptyList(), new NoStepDefinition(), uri, step);
    }

    @Override
    public void runStep(TestCaseState state) {
        throw new UndefinedStepDefinitionException();
    }

    @Override
    public void dryRunStep(TestCaseState state) {
        runStep(state);
    }

}
