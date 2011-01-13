package cucumber.runtime;

import cucumber.Cucumber;
import gherkin.formatter.PrettyFormatter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public abstract class AbstractBackendTest {
    @Test
    public void cukesFeature() throws IOException {
        assertCorrectOutput("cucumber/runtime/cukes.feature");
    }

    @Test
    public void outlineFeature() throws IOException {
        assertCorrectOutput("cucumber/runtime/outline.feature");
    }

    private void assertCorrectOutput(String featurePath) throws IOException {
        StringWriter out = new StringWriter();
        Backend backend = backend();
        Cucumber cucumber = new Cucumber(Arrays.asList(backend), new PrettyFormatter(out, true, true), new SummaryReporter(out));
        cucumber.execute(featurePath);
        String expected = getExpected(featurePath.replaceAll("feature$", "out"));
        assertEquals(expected, out.toString());
    }

    private String getExpected(String pathName) {
        final StringBuffer sb = new StringBuffer();
        Classpath.scan(pathName, new Consumer() {
            public void consume(Input input) {
                sb.append(input.getString());
            }
        });
        return sb.toString();
    }

    protected abstract Backend backend() throws IOException;
}
