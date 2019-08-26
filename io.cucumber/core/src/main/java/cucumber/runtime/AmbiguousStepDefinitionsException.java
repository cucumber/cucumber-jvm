package cucumber.runtime;

import java.util.List;

public class AmbiguousStepDefinitionsException extends CucumberException {
    private final List<StepDefinitionMatch> matches;

    public AmbiguousStepDefinitionsException(List<StepDefinitionMatch> matches) {
        super(createMessage(matches));
        this.matches = matches;
    }

    private static String createMessage(List<StepDefinitionMatch> matches) {
        StringBuilder msg = new StringBuilder();
        msg.append(matches.get(0).getStepLocation()).append(" matches more than one step definition:\n");
        for (StepDefinitionMatch match : matches) {
            msg.append("  ").append(match.getPattern()).append(" in ").append(match.getLocation()).append("\n");
        }
        return msg.toString();
    }

    public List<StepDefinitionMatch> getMatches() {
        return matches;
    }
}
