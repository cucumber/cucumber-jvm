package cucumber.runtime.ioke;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class IokeBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedOutput() {
        return "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # cucumber/runtime/ioke/stepdefs.ik\n" +
                "    Then there are 4 cukes in my belly # cucumber/runtime/ioke/stepdefs.ik\n" +
                "      java.lang.AssertionError: expected:<\"[4]\"> but was:<\"[5]\"> (ISpec ExpectationNotMet)\n" +
                "      \n" +
                "      \n" +
                "      \tat cucumber.runtime.ioke.IokeBackend.execute(IokeBackend.java:86)\n" +
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
        return new IokeBackend("cucumber/runtime/ioke");
    }
}
