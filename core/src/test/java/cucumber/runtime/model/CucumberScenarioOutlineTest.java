package cucumber.runtime.model;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CucumberScenarioOutlineTest {
    private static final List<Comment> C = new ArrayList<Comment>();

    @Test
    public void replaces_tokens_in_step_names() {
        Step outlineStep = new Step(C, null, "I have <n> cukes", 0, null, null);
        Step exampleStep = CucumberScenarioOutline.createExampleStep(outlineStep, new ExamplesTableRow(C, asList("n"), 1, ""), new ExamplesTableRow(C, asList("10"), 1, ""));
        assertEquals("I have 10 cukes", exampleStep.getName());
    }

    @Test
    public void replaces_tokens_in_doc_strings() {
        Step outlineStep = new Step(C, null, "I have <n> cukes", 0, null, new DocString(null, "I have <n> cukes", 1));

        Step exampleStep = CucumberScenarioOutline.createExampleStep(outlineStep, new ExamplesTableRow(C, asList("n"), 1, ""), new ExamplesTableRow(C, asList("10"), 1, ""));
        assertEquals("I have 10 cukes", exampleStep.getDocString().getValue());
    }

    @Test
    public void replaces_tokens_in_data_tables() {
        List<DataTableRow> rows = asList(new DataTableRow(C, asList("I", "have <n> cukes"), 1));
        Step outlineStep = new Step(C, null, "I have <n> cukes", 0, rows, null);

        Step exampleStep = CucumberScenarioOutline.createExampleStep(outlineStep, new ExamplesTableRow(C, asList("n"), 1, ""), new ExamplesTableRow(C, asList("10"), 1, ""));
        assertEquals(asList("I", "have 10 cukes"), exampleStep.getRows().get(0).getCells());
    }
}
