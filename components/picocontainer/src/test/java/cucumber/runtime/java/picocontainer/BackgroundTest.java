package cucumber.runtime.java.picocontainer;

import cucumber.Cucumber;
import cucumber.runtime.Backend;
import cucumber.runtime.java.JavaBackend;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.Reporter;
import org.junit.Test;

import java.util.Arrays;

public class BackgroundTest {
    @Test
    public void canRunFeatureWithBackground() {
        Reporter reporter = new PrettyFormatter(System.out, true, true);
        Backend backend = new JavaBackend("cucumber.runtime.java.background");
        Cucumber cucumber = new Cucumber(Arrays.asList(backend), reporter);
        cucumber.execute("cucumber/runtime/java/background");
    }

}
