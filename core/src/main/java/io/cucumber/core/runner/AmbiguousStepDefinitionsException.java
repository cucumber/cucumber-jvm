package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

final public class AmbiguousStepDefinitionsException extends CucumberException {

    private final List<PickleStepDefinitionMatch> matches;

    AmbiguousStepDefinitionsException(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        super(createMessage(step, matches));
        this.matches = matches;
    }

    private static String createMessage(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        requireNonNull(step);
        requireNonNull(matches);

        return quoteText(step.getText()) + " matches more than one step definition:\n" + matches.stream()
            .map(match -> "  " + quoteText(match.getPattern()) + " in " + match.getLocation())
            .collect(joining("\n"));
    }

    private static String quoteText(String text) {
        return "\"" + text + "\"";
    }

    public List<PickleStepDefinitionMatch> getMatches() {
        return matches;
    }

}
