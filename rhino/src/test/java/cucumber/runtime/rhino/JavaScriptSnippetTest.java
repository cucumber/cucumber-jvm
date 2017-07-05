package cucumber.runtime.rhino;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JavaScriptSnippetTest {

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "Given(/^I have (\\d+) cukes in my \"([^\"]*)\" belly$/, function(arg1, arg2) {\n" +
                "  // Write code here that turns the phrase above into concrete actions\n" +
                "  throw new Packages.cucumber.api.PendingException();\n" +
                "});\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
        return new SnippetGenerator(new JavaScriptSnippet()).getSnippet(step, "Given", null);
    }
}
