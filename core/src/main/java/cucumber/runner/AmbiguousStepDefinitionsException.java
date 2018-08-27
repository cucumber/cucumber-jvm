package cucumber.runner;

import cucumber.runtime.CucumberException;
import gherkin.pickles.PickleStep;

import java.util.List;

final public class AmbiguousStepDefinitionsException extends CucumberException {
    private final List<PickleStepDefinitionMatch> matches;

    AmbiguousStepDefinitionsException(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        super(createMessage(step, matches));
        this.matches = matches;
    }

    private static String createMessage(PickleStep step, List<PickleStepDefinitionMatch> matches) {
        StringBuilder msg = new StringBuilder();
        msg.append(quoteText(step.getText())).append(" matches more than one step definition:\n");
        for (PickleStepDefinitionMatch match : matches) {
            msg.append("  ").append(quoteText(match.getPattern())).append(" in ").append(match.getLocation()).append("\n");
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
