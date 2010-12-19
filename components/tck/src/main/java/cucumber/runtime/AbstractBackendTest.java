package cucumber.runtime;

import cucumber.Cucumber;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public abstract class AbstractBackendTest {
    @Test
    public void producesCorrectOutput() throws IOException {
        StringWriter out = new StringWriter();
        Backend backend = backend();
        Cucumber cucumber = new Cucumber(Arrays.asList(backend), new PrettyFormatter(out, true, true));
        cucumber.execute(Arrays.asList("cucumber/runtime"));
        assertEquals(expectedOutput(), out.toString());
    }

    protected abstract String expectedOutput();

    protected abstract Backend backend() throws IOException;
}
