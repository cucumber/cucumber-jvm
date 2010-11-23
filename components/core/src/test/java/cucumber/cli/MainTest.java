package cucumber.cli;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MainTest {
    @Test
    public void testShouldRunCukes() throws IOException {
        String expectedOutput = "" +
                "Feature: Cukes\n" +
                "\n" +
                "  Scenario: 1 cuke                     # cucumber/runtime/cukes.feature:2\n" +
                "    Given I have 5 cukes in my belly   # StepDefs.haveCukes(String)\n" +
                "    Then there are 4 cukes in my belly # StepDefs.checkCukes(String)\n" +
                "      junit.framework.ComparisonFailure: null expected:<[5]> but was:<[4]>\n" +
                "      \tat junit.framework.Assert.assertEquals(Assert.java:81)\n" +
                "      \tat junit.framework.Assert.assertEquals(Assert.java:87)\n" +
                "      \tat cucumber.runtime.java.StepDefs.checkCukes(StepDefs.java:17)\n" +
                "      \tat Cukes.1 cuke.Then there are 4 cukes in my belly(cucumber/runtime/cukes.feature:4)" +
                "\n" +
                "\n";

        StringWriter sw = new StringWriter();
        Main.mainWithWriter(sw,
                "--stepdefs", "cucumber.runtime.java",
                "cucumber/runtime"
        );
        System.out.println(sw);
        assertThat(sw.toString(), equalTo(expectedOutput));
    }

}
