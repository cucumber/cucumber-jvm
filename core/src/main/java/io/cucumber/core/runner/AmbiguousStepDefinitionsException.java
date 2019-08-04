package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final public class AmbiguousStepDefinitionsException extends CucumberException {

    private final List<PickleStepDefinitionMatch> matches;

    AmbiguousStepDefinitionsException(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        super(createMessage(step, matches));
        this.matches = matches;
    }

    private static String createMessage(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        if (Objects.isNull(step)) {
            throw new IllegalArgumentException("Supplied PickleStep can't be null for AmbiguousStepDefinitionsException");
        }
        if (Objects.isNull(matches)) {
            throw new IllegalArgumentException("Supplied List<PickleStepDefinitionMatch> can't be null for AmbiguousStepDefinitionsException");
        }
        final StringBuilder msg = new StringBuilder()
            .append(quoteText(step.getText()))
            .append(" matches more than one step definition:\n");
        for (final PickleStepDefinitionMatch match : matches) {
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
        return this.matches;
    }

}
