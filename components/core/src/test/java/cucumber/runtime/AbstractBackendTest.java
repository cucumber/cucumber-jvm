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
        Cucumber cucumber = new Cucumber(backend(), new PrettyFormatter(out, true, true));
        cucumber.execute(Arrays.asList("cucumber/runtime"));
//        System.out.println(out.toString());
        assertEquals(expectedOutput(), out.toString());
    }

    protected abstract String expectedOutput();

    protected abstract Backend backend() throws IOException;
}
