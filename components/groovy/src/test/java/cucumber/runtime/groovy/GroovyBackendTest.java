package cucumber.runtime.groovy;

import cucumber.runtime.*;

import java.io.IOException;

public class GroovyBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedOutput() {
        return "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # stepdefs.groovy:24\n" +
                "    Then there are 4 cukes in my belly # stepdefs.groovy:28\n" +
                "      junit.framework.ComparisonFailure: null expected:<[5]> but was:<[4]>\n" +
                "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
                "      \tat groovy.util.GroovyTestCase.assertEquals(GroovyTestCase.java:441)\n" +
                "      \tat groovy.util.GroovyTestCase$assertEquals.callStatic(Unknown Source)\n" +
                "      \tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallStatic(CallSiteArray.java:48)\n" +
                "      \tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callStatic(AbstractCallSite.java:165)\n" +
                "      \tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callStatic(AbstractCallSite.java:177)\n" +
                "      \tat cucumber.runtime.groovy.CustomWorld.checkCukes(stepdefs.groovy:16)\n" +
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
        return new GroovyBackend("cucumber/runtime/groovy");
    }
}
