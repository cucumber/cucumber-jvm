package cucumber.runtime.clojure;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class ClojureBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedOutput() {
        return "" +
                "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # stepdefs.clj:3\n" +
                "    Then there are 4 cukes in my belly # stepdefs.clj:6\n" +
                "      java.lang.AssertionError: Assert failed: (= (last-meal) (Float. expected))\n" +
                "      \tat clojure.core$eval39$fn__40.invoke(stepdefs.clj:8)\n" +
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

    @Override
    protected Backend backend() throws IOException {
        return new ClojureBackend("cucumber/runtime/clojure");
    }
}
