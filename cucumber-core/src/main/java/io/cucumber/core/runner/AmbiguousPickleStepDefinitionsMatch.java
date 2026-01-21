package io.cucumber.core.runner;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.gherkin.Step;
import io.cucumber.plugin.event.Argument;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    List<List<Argument>> getDefinitionArguments() {
        return exception.getMatches().stream()
                .map(Match::getArguments)
                .map(DefinitionArgument::createArguments)
                .collect(Collectors.toList());
    }
}
