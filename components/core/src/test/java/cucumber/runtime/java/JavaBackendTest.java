package cucumber.runtime.java;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;
import cucumber.runtime.java.pico.PicoFactory;

import java.io.IOException;

public class JavaBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedStart() {
        return "" +
                "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # StepDefs.haveCukes(String)\n" +
                "    Then there are 4 cukes in my belly # StepDefs.checkCukes(String)\n" +
                "      junit.framework.ComparisonFailure: null expected:<[5]> but was:<[4]>";
    }

    @Override
    protected String expectedEnd() {
        return "" +
                "      \tat cucumber.runtime.java.StepDefs.checkCukes(StepDefs.java:17)\n" +
                "      \tat Cukes.1 cuke.Then there are 4 cukes in my belly(cucumber/runtime/cukes.feature:4)" +
                "\n" +
                "\n";
    }

    protected Backend backend() throws IOException {
        return new JavaBackend(new PicoFactory(), new ClasspathMethodScanner(), "cucumber.runtime.java");
    }
}
