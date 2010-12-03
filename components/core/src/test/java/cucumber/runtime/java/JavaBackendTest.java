package cucumber.runtime.java;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;
import cucumber.runtime.java.pico.PicoFactory;

import java.io.IOException;

public class JavaBackendTest extends AbstractBackendTest {
    public static final String OUTPUT = "" +
            "Feature: Cukes\n" +
            "\n" +
            "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
            "    Given I have 5 cukes in my belly   # StepDefs.haveCukes(String)\n" +
            "    Then there are 4 cukes in my belly # StepDefs.checkCukes(String)\n" +
            "      junit.framework.ComparisonFailure: null expected:<[5]> but was:<[4]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.checkCukes(StepDefs.java:17)\n" +
            "      \tat Cukes.1 cuke.Then there are 4 cukes in my belly(cucumber/runtime/cukes.feature:4)\n" +
            "\n" +
            "\n" +
            "  Scenario Outline: cooking                     # cucumber/runtime/cukes.feature:6\n" +
            "    Given the <container> contains <ingredient> # cucumber/runtime/cukes.feature:7\n" +
            "    When I add <liquid>                         # cucumber/runtime/cukes.feature:8\n" +
            "    And serve it to my guests                   # cucumber/runtime/cukes.feature:9\n" +
            "    Then they'll be eating <dish>               # cucumber/runtime/cukes.feature:10\n" +
            "\n" +
            "    Examples: \n" +
            "      | container | ingredient | liquid    | dish         |\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "";

    @Override
    protected String expectedOutput() {
        return OUTPUT;
    }

    protected Backend backend() throws IOException {
        return new JavaBackend(new PicoFactory(), new ClasspathMethodScanner(), "cucumber.runtime.java");
    }
}
