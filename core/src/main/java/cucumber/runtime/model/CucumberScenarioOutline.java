package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;

import static gherkin.util.FixJava.join;

public class CucumberScenarioOutline extends CucumberFeatureElement {
    private final List<CucumberExamples> cucumberExamplesList = new ArrayList<CucumberExamples>();
    private final CucumberBackground cucumberBackground;

    public CucumberScenarioOutline(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, ScenarioOutline scenarioOutline) {
        super(cucumberFeature, scenarioOutline);
        this.cucumberBackground = cucumberBackground;
    }

    public void examples(Examples examples) {
        cucumberExamplesList.add(new CucumberExamples(this, examples));
    }

    public List<CucumberExamples> getCucumberExamplesList() {
        return cucumberExamplesList;
    }

    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<Backend> backends) {
        throw new UnsupportedOperationException();
    }

    CucumberScenario createExampleScenario(Row header, Row example, Examples examples) {
        Scenario exampleScenario = new Scenario(example.getComments(), examples.getTags(), examples.getKeyword(), exampleName(example), null, example.getLine());
        CucumberScenario cucumberScenario = new CucumberScenario(cucumberFeature, cucumberBackground, exampleScenario);
        for (Step step : getSteps()) {
            cucumberScenario.step(createExampleStep(step, header, example));
        }
        return cucumberScenario;
    }

    private String exampleName(Row example) {
        return "| " + join(example.getCells(), " | ") + " |";
    }

    private Step createExampleStep(Step step, Row header, Row example) {
        List<Integer> matchedColumns = new ArrayList<Integer>();
        String name = step.getName();

        List<String> headerCells = header.getCells();
        for (int i = 0; i < headerCells.size(); i++) {
            String headerCell = headerCells.get(i);
            String value = example.getCells().get(i);
            String token = "<" + headerCell + ">";
            if (name.contains(token)) {
                name = name.replace(token, value);
                matchedColumns.add(i);
            }
        }

        // TODO: Create CucumberStep where we can add matchedColumns
        return new Step(step.getComments(), step.getKeyword(), name, step.getLine());
    }
}
