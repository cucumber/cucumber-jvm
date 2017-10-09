package cucumber.runtime;

import gherkin.pickles.PickleStep;

import java.util.List;

public class AmbiguousStepDefinitionsException extends CucumberException {
    private final List<StepDefinitionMatch> matches;

    public AmbiguousStepDefinitionsException(PickleStep step, List<StepDefinitionMatch> matches) {
        super(createMessage(step, matches));
        this.matches = matches;
    }

    private static String createMessage(PickleStep step, List<StepDefinitionMatch> matches) {
        StringBuilder msg = new StringBuilder();
        msg.append(quoteText(step.getText())).append(" matches more than one step definition:\n");
        for (StepDefinitionMatch match : matches) {
            msg.append("  ").append(quoteText(match.getPattern())).append(" in ").append(match.getLocation()).append("\n");
        }
        return msg.toString();
    }

    private static String quoteText(String text) {
        return "\"" + text + "\"";
    }

    public List<StepDefinitionMatch> getMatches() {
        return matches;
    }
}
