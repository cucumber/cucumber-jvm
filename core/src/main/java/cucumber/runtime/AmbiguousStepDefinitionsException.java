package cucumber.runtime;

import gherkin.formatter.model.Step;

import java.util.List;

public class AmbiguousStepDefinitionsException extends CucumberException {
    private final List<StepDefinitionMatch> matches;
    private final Step step;

    public AmbiguousStepDefinitionsException(Step step, List<StepDefinitionMatch> matches) {
        super(null);
        this.step = step;
        this.matches = matches;
    }

    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder();
        msg.append(step.getKeyword()).append(step.getName()).append("(").append(matches.get(0).getStepLocation()).append(") matches more than one step definition:\n");
        for (StepDefinitionMatch match : matches) {
            msg.append("  ").append(match.getPattern()).append(" in ").append(match.getLocation()).append("\n");
        }
        return msg.toString();
    }
}
