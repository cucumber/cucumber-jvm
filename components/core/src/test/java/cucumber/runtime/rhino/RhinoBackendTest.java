package cucumber.runtime.rhino;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class RhinoBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedStart() {
        return "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # cucumber/runtime/rhino/stepdefs.js:5\n" +
                "    Then there are 4 cukes in my belly # cucumber/runtime/rhino/stepdefs.js:9\n" +
                "      org.mozilla.javascript.JavaScriptException: Expected 4, but got 5 (cucumber/runtime/rhino/stepdefs.js#11)" +
                "\n";

    }

    @Override
    protected String expectedEnd() {
        return "(cucumber/runtime/rhino/stepdefs.js:11)\n" +
                "      \tat Cukes.1 cuke.Then there are 4 cukes in my belly(cucumber/runtime/cukes.feature:4)" +
                "\n" +
                "\n";
    }

    protected Backend backend() throws IOException {
        return new RhinoBackend("cucumber/runtime/rhino");
    }
}
