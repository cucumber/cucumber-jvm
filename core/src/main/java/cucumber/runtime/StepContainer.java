package cucumber.runtime;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class StepContainer {
    private final List<Step> steps = new ArrayList<Step>();
    final CucumberFeature cucumberFeature;
    private final BasicStatement statement;

    StepContainer(CucumberFeature cucumberFeature, BasicStatement statement) {
        this.cucumberFeature = cucumberFeature;
        this.statement = statement;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void step(Step step) {
        steps.add(step);
    }

    void format(Formatter formatter) {
        statement.replay(formatter);
        for (Step step : getSteps()) {
            formatter.step(step);
        }
    }

    void runSteps(Reporter reporter, LegacyRuntime runtime) {
        for (Step step : getSteps()) {
            runStep(step, reporter, runtime);
        }
    }

    void runStep(Step step, Reporter reporter, LegacyRuntime runtime) {
        runtime.runStep(cucumberFeature.getPath(), step, reporter, cucumberFeature.getI18n());
    }
}
