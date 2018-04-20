package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runtime.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static cucumber.runtime.TestHelper.result;
import static cucumber.runtime.Utils.toURL;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public final class TestNGFormatterTest {

    @Test
    public void shouldHandleTestsBeingRunConcurrently() throws Throwable {
        final List<String> features = asList("cucumber/runtime/formatter/FormatterParallelTests.feature", "cucumber/runtime/formatter/FormatterParallelTests2.feature");

        final Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("bg_1", result("passed"));
        stepsToResult.put("bg_2", result("passed"));
        stepsToResult.put("bg_3", result("passed"));
        stepsToResult.put("step_1", result("passed"));
        stepsToResult.put("step_2", result("passed"));
        stepsToResult.put("step_3", result("passed"));
        stepsToResult.put("bg_10", result("passed"));
        stepsToResult.put("bg_20", result("passed"));
        stepsToResult.put("bg_30", result("passed"));
        stepsToResult.put("step_10", result("passed"));
        stepsToResult.put("step_20", result("passed"));
        stepsToResult.put("step_30", result("passed"));
        
        final File report = File.createTempFile("cucumber-jvm-testng", ".xml");
        TestHelper.runFormatterWithPlugin("testng", report.getAbsolutePath(), features, features.size(), stepsToResult);
        
        assertXmlEqualOr(report,"cucumber/runtime/formatter/TestNGFormatterParallelExpected1.xml", "cucumber/runtime/formatter/TestNGFormatterParallelExpected2.xml");
    }
    
    @Test
    public final void testScenarioWithUndefinedSteps() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("undefined"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, stepDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"0\" skipped=\"1\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public void testScenarioWithUndefinedStepsStrict() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("undefined"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithStrictTestNGFormatter(feature, stepsToResult, stepDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\">" +
                "                    <exception class=\"The scenario has pending or undefined step(s)\">" +
                "                        <message><![CDATA[When step...................................................................undefined\n" +
                "Then step...................................................................undefined\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[The scenario has pending or undefined step(s)]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public final void testScenarioWithPendingSteps() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step1\n" +
                "    Then step2\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step1", result("pending"));
        stepsToResult.put("step2", result("skipped"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, stepDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"0\" skipped=\"1\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public void testScenarioWithFailedSteps() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step1\n" +
                "    Then step2\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step1", result("failed", new TestNGException("message", "stacktrace")));
        stepsToResult.put("step2", result("skipped"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, stepDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\">" +
                "                    <exception class=\"cucumber.runtime.formatter.TestNGFormatterTest$TestNGException\">" +
                "                        <message><![CDATA[When step1..................................................................failed\n" +
                "Then step2..................................................................skipped\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[stacktrace]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public final void testScenarioWithPassedSteps() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("passed"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, stepDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"1\" failed=\"0\" skipped=\"0\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"PASS\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public void testScenarioWithBackground() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Background:\n" +
                "    When background\n" +
                "    Then background\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("background", result("undefined"));
        stepsToResult.put("step", result("undefined"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, stepDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"0\" skipped=\"1\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public void testScenarioOutlineWithExamples() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario Outline: scenario\n" +
                "    When step\n" +
                "    Then step\n" +
                "    Examples:\n" +
                "    | arg |\n" +
                "    |  1  |\n" +
                "    |  2  |\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("undefined"));
        long stepDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, stepDuration);
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
        CucumberFeature feature1 = TestHelper.feature("path/feature1.feature", "" +
                "Feature: feature_1\n" +
                "  Scenario: scenario_1\n" +
                "    When step\n" +
                "    Then step\n" +
                "  Scenario: scenario_2\n" +
                "    When step\n" +
                "    Then step\n");
        CucumberFeature feature2 = TestHelper.feature("path/feature2.feature", "" +
                "Feature: feature_2\n" +
                "  Scenario: scenario_3\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("passed"));
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        long stepHookDuration = milliSeconds(1);
        String actual = runFeaturesWithTestNGFormatter(Arrays.asList(feature1, feature2), stepsToResult, hooks, stepHookDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"3\" passed=\"3\" failed=\"0\" skipped=\"0\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"12\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"12\">" +
                "            <class name=\"feature_1\">" +
                "                <test-method name=\"scenario_1\" status=\"PASS\" duration-ms=\"4\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "                <test-method name=\"scenario_2\" status=\"PASS\" duration-ms=\"4\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "            </class>" +
                "            <class name=\"feature_2\">" +
                "                <test-method name=\"scenario_3\" status=\"PASS\" duration-ms=\"4\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\"/>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public void testScenarioWithFailedBeforeHook() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("skipped"));
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("before", result("failed", new TestNGException("message", "stacktrace"))));
        long stepHookDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, hooks, stepHookDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\">" +
                "                    <exception class=\"cucumber.runtime.formatter.TestNGFormatterTest$TestNGException\">" +
                "                        <message><![CDATA[When step...................................................................skipped\n" +
                "Then step...................................................................skipped\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[stacktrace]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    @Test
    public void testScenarioWithFailedAfterHook() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("step", result("passed"));
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("after", result("failed", new TestNGException("message", "stacktrace"))));
        long stepHookDuration = milliSeconds(0);
        String actual = runFeatureWithTestNGFormatter(feature, stepsToResult, hooks, stepHookDuration);
        assertXmlEqual("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"cucumber.runtime.formatter.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"yyyy-MM-ddTHH:mm:ssZ\" finished-at=\"yyyy-MM-ddTHH:mm:ssZ\">" +
                "                    <exception class=\"cucumber.runtime.formatter.TestNGFormatterTest$TestNGException\">" +
                "                        <message><![CDATA[When step...................................................................passed\n" +
                "Then step...................................................................passed\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[stacktrace]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>", actual);
    }

    private String runFeatureWithTestNGFormatter(CucumberFeature feature, Map<String, Result> stepsToResult, long stepDuration) throws Throwable {
        return runFeatureWithTestNGFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, Result>>emptyList(), stepDuration);
    }

    private String runFeatureWithTestNGFormatter(CucumberFeature feature, Map<String, Result> stepsToResult,
            List<SimpleEntry<String, Result>> hooks, long stepDuration) throws Throwable {
        return runFeaturesWithTestNGFormatter(Arrays.asList(feature), stepsToResult, hooks, stepDuration);
    }

    private String runFeaturesWithTestNGFormatter(List<CucumberFeature> features, Map<String, Result> stepsToResult,
            List<SimpleEntry<String, Result>> hooks, long stepDuration) throws Throwable {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        TestHelper.runFeaturesWithFormatter(features, stepsToResult, hooks, stepDuration, formatter);
        return new Scanner(new FileInputStream(tempFile), "UTF-8").useDelimiter("\\A").next();
    }

    private String runFeatureWithStrictTestNGFormatter(CucumberFeature feature, Map<String, Result> stepsToResult, long stepDuration) throws Throwable {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.setStrict(true);
        TestHelper.runFeatureWithFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, Result>>emptyList(), stepDuration, formatter);
        return new Scanner(new FileInputStream(tempFile), "UTF-8").useDelimiter("\\A").next();
    }
    
    private void assertXmlEqualOr(File actual, String expectedPath, String orPath) throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff expectedDiff = getDiffIgnoringDateTimes(readFile(expectedPath), new FileReader(actual));
        Diff orDiff = getDiffIgnoringDateTimes(readFile(orPath), new FileReader(actual));
        final boolean result = expectedDiff.identical() || orDiff.identical();
        assertTrue("Difference to expected:\r\n" + expectedDiff + "\r\n\r\nDifference to or:" + orDiff, result);
    }

    private Reader readFile(final String path) throws UnsupportedEncodingException {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "UTF-8");
    }

    private void assertXmlEqual(String expected, String actual) throws SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = getDiffIgnoringDateTimes(expected, actual);
        assertTrue("XML files are similar " + diff + "\nFormatterOutput = " + actual, diff.identical());
    }

    private Diff getDiffIgnoringDateTimes(final Reader expected, final Reader actual) throws IOException, SAXException {
        return new Diff(expected, actual) {
            @Override
            public int differenceFound(Difference difference) {
                if (difference.getControlNodeDetail().getNode().getNodeName().matches("started-at|finished-at")) {
                    return 0;
                }
                return super.differenceFound(difference);
            }
        };
    }
    
    private Diff getDiffIgnoringDateTimes(final String expected, final String actual) throws IOException, SAXException {
        return new Diff(expected, actual) {
            @Override
            public int differenceFound(Difference difference) {
                if (difference.getControlNodeDetail().getNode().getNodeName().matches("started-at|finished-at")) {
                    return 0;
                }
                return super.differenceFound(difference);
            }
        };
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
