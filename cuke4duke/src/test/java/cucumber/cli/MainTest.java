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
                "Feature: Hello\n" +
                "\n" +
                "  Scenario: Hi           # cucumber/runtime/fixtures/cukes.feature:2\n" +
                "    Given I have 3 cukes # CukesSteps.haveNCukes(String)\n";

        StringWriter sw = new StringWriter();

        Main main = new Main();
        main.execute(sw, "cucumber.runtime.fixtures", new String[]{"cucumber/runtime/fixtures/cukes.feature"});

        String output = sw.toString();

        System.out.println(output);

        assertThat(output, equalTo(expectedOutput));
    }

}
