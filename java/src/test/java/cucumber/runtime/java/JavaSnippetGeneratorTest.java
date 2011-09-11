package cucumber.runtime.java;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class JavaSnippetGeneratorTest {
	private List<Comment> noComments = Collections.<Comment>emptyList();
	private String keyword = "Given";
	private String name = "I have 4 cukes in my \"big\" belly";
	private int line = 0;

    @Test
    public void generatesPlainSnippet() {
		String asExpected = "" +
                "@Given(\"^I have (\\\\d+) cukes in my \\\"([^\\\"]*)\\\" belly$\")\n" +
                "public void I_have_cukes_in_my_belly(int arg1, String arg2) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
		assertThat(theGeneratedSnippet(), is(asExpected));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() throws Exception {
    	name = "before 5 after";
    	assertThat(theGeneratedSnippet(), containsString("before (\\\\d+) after"));
    }
    
	private String theGeneratedSnippet() {
		Step step = new Step(noComments, keyword, name, line);
        return new JavaSnippetGenerator(step).getSnippet();
	}
}