package io.cucumber.core.plugin;

import io.cucumber.plugin.event.Result;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.runner.TestHelper;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static io.cucumber.core.runner.TestHelper.result;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

final class TestNGFormatterTest {

    private final List<CucumberFeature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();
    private final List<Answer<Object>> hookActions = new ArrayList<>();
    private Duration stepDuration = null;

    @Test
    void testScenarioWithUndefinedSteps() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step\n" +
            "    Then step\n");
        features.add(feature);
        stepsToResult.put("step", result("undefined"));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"0\" skipped=\"1\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>" +
            "            </class>" +
            "        </test>" +
            "    </suite>" +
            "</testng-results>", actual);
    }

    @Test
    void testScenarioWithUndefinedStepsStrict() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step\n" +
            "    Then step\n");
        features.add(feature);
        stepsToResult.put("step", result("undefined"));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(true);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">" +
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
    void testScenarioWithPendingSteps() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step1\n" +
            "    Then step2\n");
        features.add(feature);
        stepsToResult.put("step1", result("pending"));
        stepsToResult.put("step2", result("skipped"));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"0\" skipped=\"1\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>" +
            "            </class>" +
            "        </test>" +
            "    </suite>" +
            "</testng-results>", actual);
    }

    @Test
    void testScenarioWithFailedSteps() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step1\n" +
            "    Then step2\n");
        features.add(feature);
        stepsToResult.put("step1", result("failed", new TestNGException("message", "stacktrace")));
        stepsToResult.put("step2", result("skipped"));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">" +
            "                    <exception class=\"io.cucumber.core.plugin.TestNGFormatterTest$TestNGException\">" +
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
    void testScenarioWithPassedSteps() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step\n" +
            "    Then step\n");
        features.add(feature);
        stepsToResult.put("step", result("passed"));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"1\" failed=\"0\" skipped=\"0\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"PASS\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>" +
            "            </class>" +
            "        </test>" +
            "    </suite>" +
            "</testng-results>", actual);
    }

    @Test
    void testScenarioWithBackground() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
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
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"0\" skipped=\"1\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>" +
            "            </class>" +
            "        </test>" +
            "    </suite>" +
            "</testng-results>", actual);
    }

    @Test
    void testScenarioOutlineWithExamples() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
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
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"2\" passed=\"0\" failed=\"0\" skipped=\"2\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"SKIP\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>" +
            "                <test-method name=\"scenario_2\" status=\"SKIP\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>" +
            "            </class>" +
            "        </test>" +
            "    </suite>" +
            "</testng-results>", actual);
    }

    @Test
    void testDurationCalculationOfStepsAndHooks() throws Throwable {
        CucumberFeature feature1 = TestFeatureParser.parse("path/feature1.feature", "" +
            "Feature: feature_1\n" +
            "  Scenario: scenario_1\n" +
            "    When step\n" +
            "    Then step\n" +
            "  Scenario: scenario_2\n" +
            "    When step\n" +
            "    Then step\n");
        CucumberFeature feature2 = TestFeatureParser.parse("path/feature2.feature", "" +
            "Feature: feature_2\n" +
            "  Scenario: scenario_3\n" +
            "    When step\n" +
            "    Then step\n");
        features.add(feature1);
        features.add(feature2);
        stepsToResult.put("step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        stepDuration = ofMillis(1);
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"3\" passed=\"3\" failed=\"0\" skipped=\"0\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"12\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"12\">" +
            "            <class name=\"feature_1\">" +
            "                <test-method name=\"scenario_1\" status=\"PASS\" duration-ms=\"4\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00.004Z\"/>" +
            "                <test-method name=\"scenario_2\" status=\"PASS\" duration-ms=\"4\" started-at=\"1970-01-01T00:00:00.004Z\" finished-at=\"1970-01-01T00:00:00.008Z\"/>" +
            "            </class>" +
            "            <class name=\"feature_2\">" +
            "                <test-method name=\"scenario_3\" status=\"PASS\" duration-ms=\"4\" started-at=\"1970-01-01T00:00:00.008Z\" finished-at=\"1970-01-01T00:00:00.012Z\"/>" +
            "            </class>" +
            "        </test>" +
            "    </suite>" +
            "</testng-results>", actual);
    }

    @Test
    void testScenarioWithFailedBeforeHook() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step\n" +
            "    Then step\n");
        features.add(feature);
        stepsToResult.put("step", result("skipped"));
        hooks.add(TestHelper.hookEntry("before", result("failed", new TestNGException("message", "stacktrace"))));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">" +
            "                    <exception class=\"io.cucumber.core.plugin.TestNGFormatterTest$TestNGException\">" +
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
    void testScenarioWithFailedAfterHook() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature\n" +
            "  Scenario: scenario\n" +
            "    When step\n" +
            "    Then step\n");
        features.add(feature);
        stepsToResult.put("step", result("passed"));
        hooks.add(TestHelper.hookEntry("after", result("failed", new TestNGException("message", "stacktrace"))));
        stepDuration = ZERO;
        String actual = runFeaturesWithFormatter(false);
        assertXmlEqual("" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
            "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
            "            <class name=\"feature\">" +
            "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">" +
            "                    <exception class=\"io.cucumber.core.plugin.TestNGFormatterTest$TestNGException\">" +
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

    private static void assertXmlEqual(String expected, String actual) {
        assertThat(actual, isIdenticalTo(expected).ignoreWhitespace());
    }

    private String runFeaturesWithFormatter(boolean strict) throws IOException {
        final File tempFile = File.createTempFile("cucumber-jvm-testng", ".xml");
        final TestNGFormatter formatter = new TestNGFormatter(tempFile.toURI().toURL());
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

    private static class TestNGException extends Exception {

        private final String stacktrace;

        TestNGException(String message, String stacktrace) {
            super(message);
            this.stacktrace = stacktrace;
        }

        @Override
        public void printStackTrace(PrintWriter printWriter) {
            printWriter.print(stacktrace);
        }
    }

}
