package cucumber.runtime.java;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JavaSnippetGeneratorTest {

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes in my \\\"([^\\\"]*)\\\" belly$\")\n" +
                "public void I_have_cukes_in_my_belly(int arg1, String arg2) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() throws Exception {
        String expected = "" +
                "@Given(\"^before (\\\\d+) after$\")\n" +
                "public void before_after(int arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
        String snippet = snippetFor("before 5 after");
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes in: my \\\"([^\\\"]*)\\\" red-belly!$\")\n" +
                "public void I_have_cukes_in_my_red_belly(int arg1, String arg2) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in: my \"big\" red-belly!"));
    }


    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
                "@Given(\"^the DI system receives a message saying \\\"([^\\\"]*)\\\"$\")\n" +
                "public void the_DI_system_receives_a_message_saying(String arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
        assertEquals(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""));
    }

    private String snippetFor(String name) {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", name, 0, null, null);
        return new JavaSnippetGenerator(step).getSnippet();
    }
}