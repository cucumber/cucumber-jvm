package cucumber.runtime;

import cucumber.Cucumber;
import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.classpath.Input;
import gherkin.formatter.PrettyPrinterOld;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public abstract class AbstractBackendTest {
    @Test
    public void simpleFeature() throws IOException {
        assertCorrectOutput("cucumber/runtime/simple.feature");
    }

    @Test
    public void outlineFeature() throws IOException {
        assertCorrectOutput("cucumber/runtime/outline.feature");
    }

    private void assertCorrectOutput(String featurePath) throws IOException {
        StringWriter out = new StringWriter();
        Backend backend = backend();
        // TODO: Pass in the right thing here.
        Cucumber cucumber = new Cucumber(Arrays.asList(backend), new PrettyPrinterOld(out, true, null), new SummaryReporter(out));
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
