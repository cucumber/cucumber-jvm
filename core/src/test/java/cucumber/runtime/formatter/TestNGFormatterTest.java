package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runner.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.AbstractMap.SimpleEntry;

import static cucumber.runner.TestHelper.result;
import static cucumber.runtime.Utils.toURL;
import static org.junit.Assert.assertTrue;

public final class TestNGFormatterTest {

    private final List<CucumberFeature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();
    private final List<Answer<Object>> hookActions = new ArrayList<>();
    private Long stepDuration = null;

    @Test
    public final void testScenarioWithUndefinedSteps() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");
        features.add(feature);
        stepsToResult.put("step", result("undefined"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("step", result("undefined"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(true);
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
        features.add(feature);
        stepsToResult.put("step1", result("pending"));
        stepsToResult.put("step2", result("skipped"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("step1", result("failed", new TestNGException("message", "stacktrace")));
        stepsToResult.put("step2", result("skipped"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("step", result("passed"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("background", result("undefined"));
        stepsToResult.put("step", result("undefined"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("step", result("undefined"));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature1);
        features.add(feature2);
        stepsToResult.put("step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        stepDuration = milliSeconds(1);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("step", result("skipped"));
        hooks.add(TestHelper.hookEntry("before", result("failed", new TestNGException("message", "stacktrace"))));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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
        features.add(feature);
        stepsToResult.put("step", result("passed"));
        hooks.add(TestHelper.hookEntry("after", result("failed", new TestNGException("message", "stacktrace"))));
        stepDuration = milliSeconds(0);
        String actual = runFeaturesWithFormatter(false);
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

    private String runFeaturesWithFormatter(boolean strict) throws IOException {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(toURL(tempFile.getAbsolutePath()));
        formatter.setStrict(strict);

        TestHelper.builder()
            .withFormatterUnderTest(formatter)
            .withFeatures(features)
            .withStepsToResult(stepsToResult)
            .withStepsToLocation(stepsToLocation)
            .withHooks(hooks)
            .withHookLocations(hookLocations)
            .withHookActions(hookActions)
            .withTimeServiceIncrement(stepDuration)
            .build()
            .run();

        return new Scanner(new FileInputStream(tempFile), "UTF-8").useDelimiter("\\A").next();
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
