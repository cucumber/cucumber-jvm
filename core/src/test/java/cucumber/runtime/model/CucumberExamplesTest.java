package cucumber.runtime.model;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class CucumberExamplesTest {
    private static final List<Comment> COMMENTS = emptyList();
    private static final List<Tag> FEATURE_TAGS = asList(new Tag("@feature", 1));
    private static final List<Tag> SO_TAGS = asList(new Tag("@scenario_outline", 1));
    private static final List<Tag> E_TAGS = asList(new Tag("@example", 1));

    @Test
    public void should_create_example_scenarios() {
        CucumberFeature cucumberFeature = new CucumberFeature(new Feature(COMMENTS, FEATURE_TAGS, "Feature", "", "", 2, "fid"), "f.feature");
        ScenarioOutline so = new ScenarioOutline(COMMENTS, SO_TAGS, "Scenario Outline", "", "", 4, "");
        CucumberScenarioOutline cso = new CucumberScenarioOutline(cucumberFeature, null, so);
        cso.step(new Step(COMMENTS, "Given ", "I have 5 <what> in my <where>", 5, null, null));
        Examples examples = new Examples(COMMENTS, E_TAGS, "Examples", "", "", 6, "", asList(
                new ExamplesTableRow(COMMENTS, asList("what", "where"), 7, ""),
                new ExamplesTableRow(COMMENTS, asList("cukes", "belly"), 8, ""),
                new ExamplesTableRow(COMMENTS, asList("apples", "basket"), 9, "")
        ));

        CucumberExamples cucumberExamples = new CucumberExamples(cso, examples);
        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        assertEquals(2, exampleScenarios.size());
        Set<Tag> expectedTags = new HashSet<Tag>();
        expectedTags.addAll(FEATURE_TAGS);
        expectedTags.addAll(SO_TAGS);
        expectedTags.addAll(E_TAGS);
        assertEquals(expectedTags, exampleScenarios.get(0).tagsAndInheritedTags());

        CucumberScenario cucumberScenario = exampleScenarios.get(0);
        Step step = cucumberScenario.getSteps().get(0);
        assertEquals("I have 5 cukes in my belly", step.getName());
    }

}
