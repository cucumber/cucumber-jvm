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
    public void runStep(TestCaseState state) throws AmbiguousStepDefinitionsException {
        throw exception;
    }

    @Override
    public void dryRunStep(TestCaseState state) throws AmbiguousStepDefinitionsException {
        runStep(state);
    }

    @Override
    public Match getMatch() {
        return exception.getMatches().get(0);
    }
}
