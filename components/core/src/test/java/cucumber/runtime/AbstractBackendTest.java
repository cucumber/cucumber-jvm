package cucumber.runtime;

import cucumber.Cucumber;
import gherkin.formatter.PrettyFormatter;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.internal.matchers.SubstringMatcher;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertThat;

public abstract class AbstractBackendTest {
    @Test
    public void producesCorrectOutput() throws IOException {
        StringWriter out = new StringWriter();
        Cucumber cucumber = new Cucumber(backend(), new PrettyFormatter(out, true, true));
        cucumber.execute(Arrays.asList("cucumber/runtime"));
        assertThat(out.toString(), startsWith(expectedStart()));
        assertThat(out.toString(), endsWith(expectedEnd()));
    }

    protected abstract String expectedStart();

    protected abstract String expectedEnd();

    private Matcher<String> startsWith(String start) {
        return new SubstringMatcher(start) {
            @Override
            protected boolean evalSubstringOf(String string) {
                return string.startsWith(substring);
            }

            @Override
            protected String relationship() {
                return "beginning with";
            }
        };
    }

    private Matcher<String> endsWith(String end) {
        return new SubstringMatcher(end) {
             @Override
             protected boolean evalSubstringOf(String string) {
                 return string.endsWith(substring);
             }

             @Override
             protected String relationship() {
                 return "ending with";
             }
         };
     }


    protected abstract Backend backend() throws IOException;
}
