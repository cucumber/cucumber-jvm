package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final public class AmbiguousStepDefinitionsException
    extends CucumberException {

    private final List<PickleStepDefinitionMatch> matches;

    AmbiguousStepDefinitionsException(final PickleStep step, final List<PickleStepDefinitionMatch> matches) {
        super(createMessage(step, Objects.isNull(matches) ? new ArrayList<>() : matches));
        this.matches = Objects.isNull(matches) ? new ArrayList<>() : matches;
    }

    private static String createMessage(final PickleStep step, final List<PickleStepDefinitionMatch> matches) {
        final StringBuilder msg = new StringBuilder();
        if (Objects.isNull(step)) {
            msg.append("Null PickleStep");
        } else {
            msg.append(quoteText(step.getText()));
        }
        msg.append(" matches more than one step definition:\n");
        for (final PickleStepDefinitionMatch match : matches) {
            msg.append("  ")
                .append(quoteText(match.getPattern()))
                .append(" in ")
                .append(match.getLocation())
                .append("\n");
        }
        return msg.toString();
    }

    private static String quoteText(final String text) {
        return "\"" + text + "\"";
    }

    public List<PickleStepDefinitionMatch> getMatches() {
        return this.matches;
    }

}
