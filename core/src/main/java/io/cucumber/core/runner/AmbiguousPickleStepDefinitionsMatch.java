package io.cucumber.core.runner;

import io.cucumber.core.feature.CucumberStep;

import java.util.Collections;

final class AmbiguousPickleStepDefinitionsMatch extends PickleStepDefinitionMatch {
    private final AmbiguousStepDefinitionsException exception;

    AmbiguousPickleStepDefinitionsMatch(String uri, CucumberStep step, AmbiguousStepDefinitionsException e) {
        super(Collections.emptyList(), new NoStepDefinition(), uri, step);
        this.exception = e;
    }

    @Override
    public void runStep(Scenario scenario) throws AmbiguousStepDefinitionsException {
        throw exception;
    }

    @Override
    public void dryRunStep(Scenario scenario) throws AmbiguousStepDefinitionsException {
        runStep(scenario);
    }

    @Override
    public Match getMatch() {
        return exception.getMatches().get(0);
    }
}
