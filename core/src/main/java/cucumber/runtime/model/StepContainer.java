package cucumber.runtime.model;

import cucumber.runtime.World;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class StepContainer {
    private final List<Step> steps = new ArrayList<Step>();
    private final BasicStatement statement;
    protected final CucumberFeature cucumberFeature;

    public StepContainer(CucumberFeature cucumberFeature, BasicStatement statement) {
        this.statement = statement;
        this.cucumberFeature = cucumberFeature;
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

    public void formatAndRunSteps(Formatter formatter, Reporter reporter, World world) {
        format(formatter);
        for (Step step : getSteps()) {
            runStep(step, reporter, world);
        }
    }

    public void runStep(Step step, Reporter reporter, World world) {
        world.runStep(cucumberFeature.getUri(), step, reporter, cucumberFeature.getLocale());
    }
}
