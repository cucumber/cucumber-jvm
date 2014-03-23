package cucumber.runtime.formatter;

import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import static cucumber.runtime.Utils.toURL;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestNGFormatterTest {

    @Test
    public final void testScenarioWithUndefinedSteps() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("undefined"));
        formatter.result(result("undefined"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithUndefinedSteps.xml", tempFile);
    }

    @Test
    public void testScenarioWithUndefinedStepsStrict() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.setStrict(true);
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("undefined"));
        formatter.result(result("undefined"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithUndefinedStepsStrict.xml", tempFile);
    }

    @Test
    public final void testScenarioWithPendingSteps() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("pending"));
        formatter.result(result("skipped"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithPendingSteps.xml", tempFile);
    }

    @Test
    public void testScenarioWithFailedSteps() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("failed", "message", "stacktrace"));
        formatter.result(result("skipped"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithFailedSteps.xml", tempFile);
    }

    @Test
    public final void testScenarioWithPassedSteps() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("passed"));
        formatter.result(result("passed"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithPassedSteps.xml", tempFile);
    }

    @Test
    public void testScenarioWithBackground() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "background"));
        formatter.step(step("keyword ", "background"));
        formatter.result(result("undefined"));
        formatter.result(result("undefined"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("undefined"));
        formatter.result(result("undefined"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithBackground.xml", tempFile);
    }

    @Test
    public void testScenarioOutlineWithExamples() throws Exception {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.scenarioOutline(scenarioOutline());
        formatter.step(step("keyword ", "outline"));
        formatter.step(step("keyword ", "outline"));
        formatter.examples(examples(3));
        formatter.startOfScenarioLifeCycle(scenario("scenario", "Scenario Outline"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("undefined"));
        formatter.result(result("undefined"));
        formatter.endOfScenarioLifeCycle(scenario("scenario", "Scenario Outline"));
        formatter.startOfScenarioLifeCycle(scenario("scenario", "Scenario Outline"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("undefined"));
        formatter.result(result("undefined"));
        formatter.endOfScenarioLifeCycle(scenario("scenario", "Scenario Outline"));
        formatter.done();
        String actual = new Scanner(new FileInputStream(tempFile), "UTF-8").useDelimiter("\\A").next();
        assertXmlEqual("" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                    "<testng-results total=\"2\" passed=\"0\" failed=\"0\" skipped=\"2\">" +
                    "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                    "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                    "            <class name=\"feature\">" +
                    "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                    "                <test-method name=\"scenario_2\" status=\"SKIP\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                    "            </class>" +
                    "        </test>" +
                    "    </suite>" +
                    "</testng-results>", actual);
    }

    @Test
    public void testDurationCalculationOfStepsAndHooks() throws Throwable {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature_1"));
        formatter.startOfScenarioLifeCycle(scenario("scenario_1"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("passed", milliSeconds(1)));
        formatter.result(result("passed", milliSeconds(1)));
        formatter.endOfScenarioLifeCycle(scenario("scenario_1"));
        formatter.startOfScenarioLifeCycle(scenario("scenario_2"));
        formatter.before(match(), result("passed", milliSeconds(1)));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("passed", milliSeconds(1)));
        formatter.result(result("passed", milliSeconds(1)));
        formatter.endOfScenarioLifeCycle(scenario("scenario_2"));
        formatter.feature(feature("feature_2"));
        formatter.startOfScenarioLifeCycle(scenario("scenario_3"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("passed", milliSeconds(1)));
        formatter.result(result("passed", milliSeconds(1)));
        formatter.after(match(), result("passed", milliSeconds(1)));
        formatter.endOfScenarioLifeCycle(scenario("scenario_3"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testDurationCalculationOfStepsAndHooks.xml", tempFile);
    }

    @Test
    public void testScenarioWithFailedBeforeHook() throws Throwable {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.before(match(), result("failed", "message", "stacktrace"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("skipped"));
        formatter.result(result("skipped"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithFailedBeforeHook.xml", tempFile);
    }

    @Test
    public void testScenarioWithFailedAfterHook() throws Throwable {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.feature(feature("feature"));
        formatter.startOfScenarioLifeCycle(scenario("scenario"));
        formatter.step(step("keyword ", "step"));
        formatter.step(step("keyword ", "step"));
        formatter.result(result("passed"));
        formatter.result(result("passed"));
        formatter.after(match(), result("failed", "message", "stacktrace"));
        formatter.endOfScenarioLifeCycle(scenario("scenario"));
        formatter.done();
        assertXmlEqual("cucumber/runtime/formatter/TestNGFormatterTest_testScenarioWithFailedAfterHook.xml", tempFile);
    }

    private void assertXmlEqual(String path, File file) throws IOException, ParserConfigurationException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        InputStreamReader inputStreamReader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "UTF-8");
        Diff diff = new Diff(inputStreamReader, new FileReader(file)) {
            @Override
            public int differenceFound(Difference difference) {
                if (difference.getControlNodeDetail().getNode().getNodeName().matches("started-at|finished-at")) {
                    return 0;
                }
                return super.differenceFound(difference);
            }
        };
        assertTrue("XML files are similar " + diff.toString(), diff.identical());
    }

    private void assertXmlEqual(String expected, String actual) throws SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(expected, actual) {
            @Override
            public int differenceFound(Difference difference) {
                if (difference.getControlNodeDetail().getNode().getNodeName().matches("started-at|finished-at")) {
                    return 0;
                }
                return super.differenceFound(difference);
            }
        };
        assertTrue("XML files are similar " + diff + "\nFormatterOutput = " + actual, diff.identical());
    }

    private Feature feature(String featureName) {
        Feature feature = mock(Feature.class);
        when(feature.getName()).thenReturn(featureName);
        return feature;
    }

    private ScenarioOutline scenarioOutline() {
        return mock(ScenarioOutline.class);
    }

    private Examples examples(int size) {
        Examples examples = mock(Examples.class);
        when(examples.getRows()).thenReturn(Arrays.asList(new ExamplesTableRow[size]));
        return examples;
    }

    private Scenario scenario(String scenarioName) {
        return scenario(scenarioName, "Scenario");
    }

    private Scenario scenario(String scenarioName, String keyword) {
        Scenario scenario = mock(Scenario.class);
        when(scenario.getKeyword()).thenReturn(keyword);
        when(scenario.getName()).thenReturn(scenarioName);
        return scenario;
    }

    private Step step(String keyword, String stepName) {
        Step step = mock(Step.class);
        when(step.getKeyword()).thenReturn(keyword);
        when(step.getName()).thenReturn(stepName);
        return step;
    }

    private Match match() {
        return mock(Match.class);
    }

    private Result result(String status) {
        return new Result(status, null, null);
    }

    private Result result(String status, Long duration) {
        return new Result(status, duration, null);
    }

    private Result result(String status, String message, String stacktrace) {
        return new Result(status, null, new TestNGException(message, stacktrace), null);
    }

    private Long milliSeconds(int milliSeconds) {
        return milliSeconds * 1000000L;
    }

    private static class TestNGException extends Exception {

        private final String stacktrace;

        public TestNGException(String message, String stacktrace) {
            super(message);
            this.stacktrace = stacktrace;
        }

        @Override
        public void printStackTrace(PrintWriter printWriter) {
            printWriter.print(stacktrace);
        }
    }
}
