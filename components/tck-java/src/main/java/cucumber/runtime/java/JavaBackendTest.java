package cucumber.runtime.java;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public abstract class JavaBackendTest extends AbstractBackendTest {
    public static final String OUTPUT = "" +
            "Feature: Cukes\n" +
            "\n" +
            "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
            "    Given I have 5 cukes in my belly   # StepDefs.haveCukes(String)\n" +
            "    Then there are 4 cukes in my belly # StepDefs.checkCukes(String)\n" +
            "      junit.framework.ComparisonFailure: null expected:<[5]> but was:<[4]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.checkCukes(StepDefs.java:23)\n" +
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
            "      junit.framework.ComparisonFailure: null expected:<[glass]> but was:<[bowl]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.containerContainsIngredient(StepDefs.java:28)\n" +
            "      \tat Cukes.cooking.Given the bowl contains oats(cucumber/runtime/cukes.feature:7)\n" +
            "\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      junit.framework.ComparisonFailure: null expected:<[glass]> but was:<[bowl]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.containerContainsIngredient(StepDefs.java:28)\n" +
            "      \tat Cukes.cooking.Given the bowl contains oats(cucumber/runtime/cukes.feature:7)\n" +
            "\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      junit.framework.ComparisonFailure: null expected:<[glass]> but was:<[bowl]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.containerContainsIngredient(StepDefs.java:28)\n" +
            "      \tat Cukes.cooking.Given the bowl contains oats(cucumber/runtime/cukes.feature:7)\n" +
            "\n" +
            "      | bowl      | oats       | milk      | oatmeal      |\n" +
            "      junit.framework.ComparisonFailure: null expected:<[glass]> but was:<[bowl]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.containerContainsIngredient(StepDefs.java:28)\n" +
            "      \tat Cukes.cooking.Given the bowl contains oats(cucumber/runtime/cukes.feature:7)\n" +
            "\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      junit.framework.ComparisonFailure: null expected:<[milk]> but was:<[champagne]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.addLiquid(StepDefs.java:33)\n" +
            "      \tat Cukes.cooking.When I add champagne(cucumber/runtime/cukes.feature:8)\n" +
            "\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      junit.framework.ComparisonFailure: null expected:<[milk]> but was:<[champagne]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.addLiquid(StepDefs.java:33)\n" +
            "      \tat Cukes.cooking.When I add champagne(cucumber/runtime/cukes.feature:8)\n" +
            "\n" +
            "      | glass     | guinness   | champagne | black velvet |\n" +
            "      junit.framework.ComparisonFailure: null expected:<[milk]> but was:<[champagne]>\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
            "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
            "      \tat cucumber.runtime.java.StepDefs.addLiquid(StepDefs.java:33)\n" +
            "      \tat Cukes.cooking.When I add champagne(cucumber/runtime/cukes.feature:8)\n" +
            "\n";

    @Override
    protected String expectedOutput() {
        return OUTPUT;
    }

    protected Backend backend() throws IOException {
        return new JavaBackend("cucumber.runtime.java");
    }
}
