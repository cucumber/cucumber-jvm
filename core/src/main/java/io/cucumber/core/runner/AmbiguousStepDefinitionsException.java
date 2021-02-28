package io.cucumber.core.runner;

import io.cucumber.core.gherkin.Step;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

final class AmbiguousStepDefinitionsException extends Exception {

    private final List<PickleStepDefinitionMatch> matches;

    AmbiguousStepDefinitionsException(Step step, List<PickleStepDefinitionMatch> matches) {
        super(createMessage(step, matches));
        this.matches = matches;
    }

    private static String createMessage(Step step, List<PickleStepDefinitionMatch> matches) {
        requireNonNull(step);
        requireNonNull(matches);

        return quoteText(step.getText()) + " matches more than one step definition:\n" + matches.stream()
                .map(match -> "  " + quoteText(match.getPattern()) + " in " + match.getLocation())
                .collect(joining("\n"));
    }

    private static String quoteText(String text) {
        return "\"" + text + "\"";
    }

    List<PickleStepDefinitionMatch> getMatches() {
        return matches;
    }

}
