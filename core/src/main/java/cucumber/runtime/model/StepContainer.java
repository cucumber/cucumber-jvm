package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


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

    void runSteps(Reporter reporter, Runtime runtime, Set<Tag> tags) {
        for (Step step : getSteps()) {
            String stepResult = runStep(step, reporter, runtime);
            runtime.runAfterStepHooks(reporter, tags, stepResult);
        }
    }

    String runStep(Step step, Reporter reporter, Runtime runtime) {
        return runtime.runStep(cucumberFeature.getPath(), step, reporter, cucumberFeature.getI18n());
    }
}
