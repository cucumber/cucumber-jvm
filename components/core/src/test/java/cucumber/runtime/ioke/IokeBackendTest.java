package cucumber.runtime.ioke;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class IokeBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedStart() {
        return "" +
                "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # cucumber/runtime/ioke/stepdefs.ik\n" +
                "    Then there are 4 cukes in my belly # cucumber/runtime/ioke/stepdefs.ik\n" +
                "      java.lang.AssertionError: expected:<\"[4]\"> but was:<\"[5]\"> (ISpec ExpectationNotMet)\n" +
                "      \n" +
                "      \n";

    }

    @Override
    protected String expectedEnd() {
        return "" +
                "      \tat cucumber.runtime.ioke.IokeStepDefinition.execute(IokeStepDefinition.java:95)\n" +
                "      \tat Cukes.1 cuke.Then there are 4 cukes in my belly(cucumber/runtime/cukes.feature:4)" +
                "\n" +
                "\n";
    }

    protected Backend backend() throws IOException {
        return new IokeBackend("cucumber/runtime/ioke");
    }
}
