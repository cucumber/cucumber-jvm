package cucumber.runtime.model;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

import cucumber.runtime.World;
import cucumber.table.NoOpTableHeaderMapper;
import cucumber.table.Table;
import cucumber.table.TableConverter;

public class CucumberScenarioOutline extends AbstractFeatureElement {
    private final ScenarioOutline scenarioOutline;
    private Examples examples;

    public CucumberScenarioOutline(CucumberFeature cucumberFeature, String uri, ScenarioOutline scenarioOutline) {
        super(cucumberFeature, uri);
        this.scenarioOutline = scenarioOutline;
    }

    public ScenarioOutline getScenarioOutline() {
        return scenarioOutline;
    }

    public void examples(Examples examples) {
        this.examples = examples;
    }

    public void run(World world, Formatter formatter, Reporter reporter, List<Step> stepsToRun) {
        formatter.scenarioOutline(scenarioOutline);
        for (Step step : stepsToRun) {
            formatter.step(step);
        }
        Table table = new Table(examples.getRows(), new TableConverter(new XStream()), new NoOpTableHeaderMapper());
        formatter.examples(examples);
        for (Map<String, String> map : table.hashes()) {
            for (Step step : stepsToRun) {
                world.runOutlineStep(getUri(), getOutlineStep(step, map), reporter, getCucumberFeature().getLocale());
            }
        }
    }

    public Set<String> tags() {
        Set<String> tags = getCucumberFeature().tags();
        for (Tag tag : scenarioOutline.getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }

    private Step getOutlineStep(Step step, Map<String, String> values) {
        String name = step.getName();
        for (Argument arg : step.getOutlineArgs()) {
            String argHeader = arg.getVal().substring(1, arg.getVal().length() - 1);
            name.replace(arg.getVal(), values.get(argHeader));
        }
        Step outlineStep = new Step(step.getComments(), step.getKeyword(), name, step.getLine());
        outlineStep.setMultilineArg(step.getMultilineArg());
        return outlineStep;
    }
}
