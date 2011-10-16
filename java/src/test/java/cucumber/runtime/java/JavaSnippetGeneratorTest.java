package cucumber.runtime.java;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;

import java.util.Collections;

import org.junit.Test;

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
    
    private String snippetFor(String name) {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", name, 0);
        return new JavaSnippetGenerator(step).getSnippet();
	}
}