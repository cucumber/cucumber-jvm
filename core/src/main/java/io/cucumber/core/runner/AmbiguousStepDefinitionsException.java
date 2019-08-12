package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

final public class AmbiguousStepDefinitionsException extends CucumberException {

    private final List<PickleStepDefinitionMatch> matches;

    AmbiguousStepDefinitionsException(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        super(createMessage(step, matches));
        this.matches = matches;
    }

    private static String createMessage(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        requireNonNull(step);
        requireNonNull(matches);

        StringBuilder msg = new StringBuilder()
            .append(quoteText(step.getText()))
            .append(" matches more than one step definition:\n");
        for (PickleStepDefinitionMatch match : matches) {
            msg.append("  ")
                .append(quoteText(match.getPattern()))
                .append(" in ")
                .append(match.getLocation())
                .append("\n");
        }
        return msg.toString();
    }

    private static String quoteText(String text) {
        return "\"" + text + "\"";
    }

    public List<PickleStepDefinitionMatch> getMatches() {
        return matches;
    }

}
