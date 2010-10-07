package cucumber;

import cucumber.runtime.java.MethodStepDefinition;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.StringWriter;
import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RuntimeTest {
    private String have3Cukes = "" +
            "Feature: Hello\n" +
            "\n" +
            "  Scenario: Hi\n" +
            "    Given I have 3 cukes\n" +
            "";

    private static class CukeSteps {
        public void haveNCukes(String n) {

        }
    }

    @Test
    public void testShouldPrintSimpleResults() throws NoSuchMethodException {
        String expectedOutput = "" +
                "Feature: Hello\n" +
                "\n" +
                "  Scenario: Hi           # features/hello.feature:3\n" +
                "    Given I have 3 cukes # RuntimeTest$CukeSteps.haveNCukes(String)\n" +
                "";

        assertOutput(have3Cukes, expectedOutput, "haveNCukes");
    }

    private void assertOutput(String source, String expectedOutput, String methodName) throws NoSuchMethodException {
        StepDefinition haveCukes = stepDefinition(methodName);

        StringWriter output = new StringWriter();
        PrettyFormatter pretty = new PrettyFormatter(output, false);

        Runtime runtime = new Runtime(asList(haveCukes), pretty);

        FeatureSource helloFeature = new FeatureSource(source, "features/hello.feature");
        runtime.execute(helloFeature);

        assertThat(output.toString(), equalTo(expectedOutput));
    }

    private StepDefinition stepDefinition(String methodName) throws NoSuchMethodException {
        Method method = CukeSteps.class.getDeclaredMethod(methodName, String.class);
        return new MethodStepDefinition(method, new CukeSteps());
    }

}
