package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;

public class CucumberScenarioOutline extends CucumberTagStatement {
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
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<Backend> backends, List<String> gluePaths) {
        format(formatter);
        for (CucumberExamples cucumberExamples : cucumberExamplesList) {
            cucumberExamples.format(formatter);
            List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
            for (CucumberScenario exampleScenario : exampleScenarios) {
                exampleScenario.run(formatter, reporter, runtime, backends, gluePaths);
            }
        }
    }

    CucumberScenario createExampleScenario(Row header, Row example, Examples examples) {
        Scenario exampleScenario = new Scenario(example.getComments(), examples.getTags(), tagStatement.getKeyword(), tagStatement.getName(), null, example.getLine());
        CucumberScenario cucumberScenario = new CucumberScenario(cucumberFeature, cucumberBackground, exampleScenario, example);
        for (Step step : getSteps()) {
            cucumberScenario.step(createExampleStep(step, header, example));
        }
        return cucumberScenario;
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

        // TODO: Create CucumberStep where we can add matchedColumns. This allows us
        // to colour individual cells differently
        return new Step(step.getComments(), step.getKeyword(), name, step.getLine());
    }
}
