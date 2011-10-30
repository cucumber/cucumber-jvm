package cucumber.runtime.model;

import gherkin.formatter.model.*;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class CucumberExamplesTest {
    private static final List<Comment> COMMENTS = emptyList();
    private static final List<Tag> TAGS = emptyList();

    @Test
    public void should_create_example_scenarios() {
        ScenarioOutline so = new ScenarioOutline(COMMENTS, TAGS, "Scenario Outline", "", "", 1, "");
        CucumberScenarioOutline cso = new CucumberScenarioOutline(null, null, so);
        cso.step(new Step(COMMENTS, "Given ", "I have 5 <what> in my <where>", 2, null, null));
        Examples examples = new Examples(COMMENTS, TAGS, "Examples", "", "", 3, "", asList(
                new ExamplesTableRow(COMMENTS, asList("what", "where"), 4, ""),
                new ExamplesTableRow(COMMENTS, asList("cukes", "belly"), 5, ""),
                new ExamplesTableRow(COMMENTS, asList("apples", "basket"), 6, "")
        ));

        CucumberExamples cucumberExamples = new CucumberExamples(cso, examples);
        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        assertEquals(2, exampleScenarios.size());

        CucumberScenario cucumberScenario = exampleScenarios.get(0);
        Step step = cucumberScenario.getSteps().get(0);
        assertEquals("I have 5 cukes in my belly", step.getName());
    }

}
