package cucumber.runtime.rhino;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class RhinoBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedOutput() {
        return "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # cucumber/runtime/rhino/stepdefs.rhino:5\n" +
                "    Then there are 4 cukes in my belly # cucumber/runtime/rhino/stepdefs.rhino:9\n" +
                "      org.mozilla.javascript.JavaScriptException: Expected 4, but got 5 (cucumber/runtime/rhino/stepdefs.rhino#11)\n" +
                "      \tat org.mozilla.javascript.gen.c2._c3(cucumber/runtime/rhino/stepdefs.rhino:11)\n" +
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

    }

    protected Backend backend() throws IOException {
        return new RhinoBackend("cucumber/runtime/rhino");
    }
}
