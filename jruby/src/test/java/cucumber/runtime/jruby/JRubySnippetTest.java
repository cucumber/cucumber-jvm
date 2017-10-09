package cucumber.runtime.jruby;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JRubySnippetTest {

    private static final List<Argument> NO_ARGUMENTS = Collections.emptyList();
    private static final List<PickleLocation> NO_LOCATIONS = Collections.emptyList();

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "Given /^I have (\\d+) cukes in my \"([^\"]*)\" belly$/ do |arg1, arg2|\n" +
                "  # Write code here that turns the phrase above into concrete actions\n" +
                "  pending\n" +
                "end\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, NO_ARGUMENTS, NO_LOCATIONS);
        return new SnippetGenerator(new JRubySnippet()).getSnippet(step, "Given", null);
    }
}
