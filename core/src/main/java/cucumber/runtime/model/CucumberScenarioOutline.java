package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<? extends Backend> backends, List<String> gluePaths) {
        format(formatter);
        for (CucumberExamples cucumberExamples : cucumberExamplesList) {
            cucumberExamples.format(formatter);
            List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
            for (CucumberScenario exampleScenario : exampleScenarios) {
                exampleScenario.run(formatter, reporter, runtime, backends, gluePaths);
            }
        }
    }

    CucumberScenario createExampleScenario(ExamplesTableRow header, ExamplesTableRow example, List<Tag> tags) {
        Scenario exampleScenario = new Scenario(example.getComments(), tags, tagStatement.getKeyword(), tagStatement.getName(), "", example.getLine(), example.getId());
        CucumberScenario cucumberScenario = new CucumberScenario(cucumberFeature, cucumberBackground, exampleScenario, example);
        for (Step step : getSteps()) {
            cucumberScenario.step(createExampleStep(step, header, example));
        }
        return cucumberScenario;
    }

    static ExampleStep createExampleStep(Step step, ExamplesTableRow header, ExamplesTableRow example) {
        Set<Integer> matchedColumns = new HashSet<Integer>();
        List<String> headerCells = header.getCells();
        List<String> exampleCells = example.getCells();

        // Create a step with replaced tokens
        String name = replaceTokens(matchedColumns, headerCells, exampleCells, step.getName());

        return new ExampleStep(
                step.getComments(),
                step.getKeyword(),
                name,
                step.getLine(),
                rowsWithTokensReplaced(step.getRows(), headerCells, exampleCells, matchedColumns),
                docStringWithTokensReplaced(step.getDocString(), headerCells, exampleCells, matchedColumns),
                matchedColumns);
    }

    private static List<DataTableRow> rowsWithTokensReplaced(List<DataTableRow> rows, List<String> headerCells, List<String> exampleCells, Set<Integer> matchedColumns) {
        if (rows != null) {
            List<DataTableRow> newRows = new ArrayList<DataTableRow>(rows.size());
            for (Row row : rows) {
                List<String> newCells = new ArrayList<String>(row.getCells().size());
                for (String cell : row.getCells()) {
                    newCells.add(replaceTokens(matchedColumns, headerCells, exampleCells, cell));
                }
                newRows.add(new DataTableRow(row.getComments(), newCells, row.getLine()));
            }
            return newRows;
        } else {
            return null;
        }
    }

    private static DocString docStringWithTokensReplaced(DocString docString, List<String> headerCells, List<String> exampleCells, Set<Integer> matchedColumns) {
        if (docString != null) {
            String docStringValue = replaceTokens(matchedColumns, headerCells, exampleCells, docString.getValue());
            return new DocString(docString.getContentType(), docStringValue, docString.getLine());
        } else {
            return null;
        }
    }

    private static String replaceTokens(Set<Integer> matchedColumns, List<String> headerCells, List<String> exampleCells, String text) {
        for (int i = 0; i < headerCells.size(); i++) {
            String headerCell = headerCells.get(i);
            String value = exampleCells.get(i);
            String token = "<" + headerCell + ">";
            if (text.contains(token)) {
                text = text.replace(token, value);
                matchedColumns.add(i);
            }
        }
        return text;
    }
}
