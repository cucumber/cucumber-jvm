package cucumber.runtime;

import cucumber.FeatureSource;
import cucumber.StepDefinition;
import cucumber.runtime.java.MethodStepDefinition;
import cucumber.runtime.java.pico.PicoFactory;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ExecutorTest {
    private String have3Cukes = "" +
            "Feature: Hello\n" +
            "\n" +
            "  Scenario: Hi\n" +
            "    Given I have 3 cukes\n" +
            "";

    public static class CukeSteps {
        public void haveNCukes(String n) {

        }

        public void haveNCukesAndFail(String n) {
            badStuff();
        }

        private void badStuff() {
            throw new RuntimeException("Oh noes");
        }
    }

    @Test
    public void testShouldPrintSimpleResults() throws NoSuchMethodException {
        String expectedOutput = "" +
                "Feature: Hello\n" +
                "\n" +
                "  Scenario: Hi           # features/hello.feature:3\n" +
                "    Given I have 3 cukes # ExecutorTest$CukeSteps.haveNCukes(String)\n" +
                "";

        assertOutput(have3Cukes, Pattern.compile("I have (\\d+) cukes"), "haveNCukes", expectedOutput);
    }

    @Test
    public void testShouldPrintResultsWithErrors() throws NoSuchMethodException {
        String expectedOutput = "" +
                "Feature: Hello\n" +
                "\n" +
                "  Scenario: Hi           # features/hello.feature:3\n" +
                "    Given I have 3 cukes # ExecutorTest$CukeSteps.haveNCukesAndFail(String)\n" +
                "      java.lang.RuntimeException: Oh noes\n" +
                "      \tat cucumber.runtime.ExecutorTest$CukeSteps.badStuff(ExecutorTest.java:36)\n" +
                "      \tat cucumber.runtime.ExecutorTest$CukeSteps.haveNCukesAndFail(ExecutorTest.java:32)\n" +
                "      \tat Hello.Hi.Given I have 3 cukes(features/hello.feature:4)\n" +
                "\n";

        assertOutput(have3Cukes, Pattern.compile("I have (\\d+) cukes"), "haveNCukesAndFail", expectedOutput);
    }

    private void assertOutput(String source, Pattern pattern, String methodName, String expectedOutput) throws NoSuchMethodException {
        StepDefinition haveCukes = stepDefinition(pattern, methodName);

        StringWriter output = new StringWriter();
        PrettyFormatter pretty = new PrettyFormatter(output, false);

        Executor runtime = new Executor(asList(haveCukes), pretty);

        FeatureSource helloFeature = new FeatureSource(source, "features/hello.feature");
        runtime.execute(helloFeature);

        assertThat(output.toString(), equalTo(expectedOutput));
    }

    private StepDefinition stepDefinition(Pattern pattern, String methodName) throws NoSuchMethodException {
        Method method = CukeSteps.class.getDeclaredMethod(methodName, String.class);
        PicoFactory objectFactory = new PicoFactory();
        objectFactory.addClass(method.getDeclaringClass());
        objectFactory.createObjects();
        return new MethodStepDefinition(pattern, method, objectFactory);
    }

}
