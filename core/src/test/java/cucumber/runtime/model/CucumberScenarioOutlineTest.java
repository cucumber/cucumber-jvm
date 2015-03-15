package cucumber.runtime.model;

import cucumber.runtime.CucumberException;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CucumberScenarioOutlineTest {
    private static final List<Comment> C = new ArrayList<Comment>();
    private static final List<Tag> T =  Collections.<Tag>emptyList();

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

    @Test(expected=CucumberException.class)
    public void does_not_allow_the_step_to_be_empty_after_replacement() {
        Step outlineStep = new Step(C, null, "<step>", 0, null, null);

        CucumberScenarioOutline.createExampleStep(outlineStep, new ExamplesTableRow(C, asList("step"), 1, ""), new ExamplesTableRow(C, asList(""), 1, ""));
    }

    @Test
    public void allows_doc_strings_to_be_empty_after_replacement() {
        Step outlineStep = new Step(C, null, "Some step", 0, null, new DocString(null, "<doc string>", 1));

        Step exampleStep = CucumberScenarioOutline.createExampleStep(outlineStep, new ExamplesTableRow(C, asList("doc string"), 1, ""), new ExamplesTableRow(C, asList(""), 1, ""));

        assertEquals("", exampleStep.getDocString().getValue());
    }

    @Test
    public void allows_data_table_entries_to_be_empty_after_replacement() {
        List<DataTableRow> rows = asList(new DataTableRow(C, asList("<entry>"), 1));
        Step outlineStep = new Step(C, null, "Some step", 0, rows, null);

        Step exampleStep = CucumberScenarioOutline.createExampleStep(outlineStep, new ExamplesTableRow(C, asList("entry"), 1, ""), new ExamplesTableRow(C, asList(""), 1, ""));

        assertEquals(asList(""), exampleStep.getRows().get(0).getCells());
    }

    /***
     * From a scenario outline, we create one or more "Example Scenario"s. This is composed
     * of each step from the outline, with the tokens replaced with the pertient values 
     * for the current example row. <p />
     * 
     * Each "Example Scenario" has a name. This was previously just a copy of the outline's
     * name. However, we'd like to be able to support token replacement in the scenario too,
     * for example:
     * 
     * <pre>
     * Scenario Outline: Time offset check for <LOCATION_NAME>
     * Given my local country is <LOCATION_NAME>
     * When I compare the time difference to GMT
     * Then the time offset should be <OFFSET>
     *  
     * Examples: 
     * | LOCATION_NAME | OFFSET |
     * | London        | 1      |
     * | San Fran      | 8      |
     * </pre>
     * 
     * Will create a scenario named "Time offset check for London" for the first row in the 
     * examples table.
     */
    @Test
    public void replaces_tokens_in_scenario_names() {
        // Create Gherkin the outline itself ...
        ScenarioOutline outline = new ScenarioOutline(C, T,"Scenario Outline", "Time offset check for <LOCATION_NAME>", "", new Integer(1), "");

        // ... then the Cukes implementation
        CucumberScenarioOutline cukeOutline = new CucumberScenarioOutline(null, null, outline);
        CucumberScenario exampleScenario = cukeOutline.createExampleScenario(new ExamplesTableRow(C, asList("LOCATION_NAME"), 1, ""), new ExamplesTableRow(C, asList("London"), 1, ""), T, "");

        assertEquals("Time offset check for London", exampleScenario.getGherkinModel().getName());
    }  
}
