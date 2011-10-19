package cucumber.runtime.model;

import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.List;

public class CucumberExamples {
    private final CucumberScenarioOutline cucumberScenarioOutline;
    private final Examples examples;

    public CucumberExamples(CucumberScenarioOutline cucumberScenarioOutline, Examples examples) {
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        this.examples = examples;
    }

    public List<CucumberScenario> createExampleScenarios() {
        List<CucumberScenario> exampleScenarios = new ArrayList<CucumberScenario>();

        List<Row> rows = examples.getRows();
        for (int i = 1; i < rows.size(); i++) {
            exampleScenarios.add(cucumberScenarioOutline.createExampleScenario(rows.get(0), rows.get(i), examples));
        }
        return exampleScenarios;
    }

    public Examples getExamples() {
        return examples;
    }
}
