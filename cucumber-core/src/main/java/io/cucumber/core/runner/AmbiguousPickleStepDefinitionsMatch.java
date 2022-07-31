package io.cucumber.core.runner;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.gherkin.Step;

import java.net.URI;
import java.util.Collections;

final class AmbiguousPickleStepDefinitionsMatch extends PickleStepDefinitionMatch {

    private final AmbiguousStepDefinitionsException exception;

    AmbiguousPickleStepDefinitionsMatch(URI uri, Step step, AmbiguousStepDefinitionsException e) {
        super(Collections.emptyList(), new NoStepDefinition(), uri, step);
        this.exception = e;
    }

    @Override
    public void runStep(TestCaseState state) throws AmbiguousStepDefinitionsException {
        throw exception;
    }

    @Override
    public void dryRunStep(TestCaseState state) throws AmbiguousStepDefinitionsException {
        throw exception;
    }

}
