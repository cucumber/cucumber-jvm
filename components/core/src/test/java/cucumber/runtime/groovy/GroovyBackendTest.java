package cucumber.runtime.groovy;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GroovyBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedStart() {
        return "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # stepdefs.groovy:24\n" +
                "    Then there are 4 cukes in my belly # stepdefs.groovy:28\n" +
                "      junit.framework.ComparisonFailure: null expected:<[5]> but was:<[4]>";

    }

    @Override
    protected String expectedEnd() {
        return "" +
                "      \tat cucumber.runtime.groovy.CustomWorld.checkCukes(stepdefs.groovy:16)\n" +
                "      \tat Cukes.1 cuke.Then there are 4 cukes in my belly(cucumber/runtime/cukes.feature:4)" +
                "\n" +
                "\n";
    }

    protected Backend backend() throws IOException {
        List<GroovyBackend.Script> scripts = Arrays.asList(script("cucumber/runtime/groovy/stepdefs.groovy"));
        return new GroovyBackend(scripts);
    }

    private GroovyBackend.Script script(String path) throws IOException {
        return new GroovyBackend.Script(reader(path), path);
    }

}
