package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class StepContainer {
    private final List<Step> steps = new ArrayList<Step>();
    private final BasicStatement statement;

    public StepContainer(BasicStatement statement) {
        this.statement = statement;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void step(Step step) {
        steps.add(step);
    }

    public void format(Formatter formatter) {
        statement.replay(formatter);
        for (Step step : getSteps()) {
            formatter.step(step);
        }
    }
}
