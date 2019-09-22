package io.cucumber.core.plugin;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.plugin.event.Result;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.opentest4j.TestAbortedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static io.cucumber.core.runner.TestHelper.result;
import static java.time.Duration.ZERO;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;
import static org.xmlunit.matchers.ValidationMatcher.valid;

class JUnitFormatterTest {

    private final List<CucumberFeature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();
    private final List<Answer<Object>> hookActions = new ArrayList<>();
    private Duration stepDuration = null;
    private boolean strict = false;

    private static void assertXmlEqual(Object expected, Object actual) throws IOException {
        assertThat(actual, isIdenticalTo(expected).ignoreWhitespace());
        assertThat(actual, valid(JUnitFormatterTest.class.getResourceAsStream("/io/cucumber/core/plugin/surefire-test-report-3.0.xsd")));
    }

    @Test
    void featureSimpleTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(singletonList("classpath:io/cucumber/core/plugin//JUnitFormatterTest_1.feature"));
        assertXmlEqual(JUnitFormatterTest.class.getResourceAsStream("/io/cucumber/core/plugin/JUnitFormatterTest_1.report.xml"), report);
    }

    @Test
    void featureWithBackgroundTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(singletonList("classpath:io/cucumber/core/plugin//JUnitFormatterTest_2.feature"));
        assertXmlEqual(JUnitFormatterTest.class.getResourceAsStream("/io/cucumber/core/plugin/JUnitFormatterTest_2.report.xml"), report);
    }

    @Test
    void featureWithOutlineTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(singletonList("classpath:io/cucumber/core/plugin//JUnitFormatterTest_3.feature"));
        assertXmlEqual(JUnitFormatterTest.class.getResourceAsStream("/io/cucumber/core/plugin/JUnitFormatterTest_3.report.xml"), report);
    }

    @Test
    void featureSimpleStrictTest() throws Exception {
        boolean strict = true;
        File report = runFeaturesWithJunitFormatter(singletonList("classpath:io/cucumber/core/plugin//JUnitFormatterTest_1.feature"), strict);
        assertXmlEqual(JUnitFormatterTest.class.getResourceAsStream("/io/cucumber/core/plugin/JUnitFormatterTest_1_strict.report.xml"), report);
    }

    @Test
    void should_format_passed_scenario() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0.003\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step............................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_empty_scenario() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n");
        features.add(feature);
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"1\" errors=\"0\" tests=\"1\" time=\"0\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
            "        <skipped message=\"The scenario has no steps\"/>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_empty_scenario_strict() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n");
        features.add(feature);
        stepDuration = Duration.ofMillis(1L);
        strict = true;

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"1\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
            "        <failure message=\"The scenario has no steps\" type=\"java.lang.Exception\"/>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_skipped_scenario() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        Throwable exception = new TestAbortedException("message");
        stepsToResult.put("first step", result("skipped", exception));
        stepsToResult.put("second step", result("skipped"));
        stepsToResult.put("third step", result("skipped"));
        stepDuration = Duration.ofMillis(1);

        String formatterOutput = runFeaturesWithFormatter();

        String stackTrace = getStackTrace(exception);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"1\" errors=\"0\" tests=\"1\" time=\"0.003\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.003\">\n" +
            "        <skipped message=\"" + stackTrace.replace("\n\t", "&#10;&#9;").replaceAll("\r", "&#13;") + "\"><![CDATA[" +
            "Given first step............................................................skipped\n" +
            "When second step............................................................skipped\n" +
            "Then third step.............................................................skipped\n" +
            "\n" +
            "StackTrace:\n" +
            stackTrace +
            "]]></skipped>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_pending_scenario() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("pending"));
        stepsToResult.put("second step", result("skipped"));
        stepsToResult.put("third step", result("undefined"));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"1\" errors=\"0\" tests=\"1\" time=\"0.003\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.003\">\n" +
            "        <skipped><![CDATA[" +
            "Given first step............................................................pending\n" +
            "When second step............................................................skipped\n" +
            "Then third step.............................................................undefined\n" +
            "]]></skipped>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_failed_scenario() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("failed"));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"1\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0.003\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.003\">\n" +
            "        <failure message=\"the message\" type=\"io.cucumber.core.runner.TestHelper$1MockedTestAbortedException\"><![CDATA[" +
            "Given first step............................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................failed\n" +
            "\n" +
            "StackTrace:\n" +
            "the stack trace]]></failure>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_handle_failure_in_before_hook() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("failed")));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"1\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0.004\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.004\">\n" +
            "        <failure message=\"the message\" type=\"io.cucumber.core.runner.TestHelper$1MockedTestAbortedException\"><![CDATA[" +
            "Given first step............................................................skipped\n" +
            "When second step............................................................skipped\n" +
            "Then third step.............................................................skipped\n" +
            "\n" +
            "StackTrace:\n" +
            "the stack trace" +
            "]]></failure>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_handle_pending_in_before_hook() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("skipped"));
        stepsToResult.put("second step", result("skipped"));
        stepsToResult.put("third step", result("skipped"));
        hooks.add(TestHelper.hookEntry("before", result("pending")));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"1\" errors=\"0\" tests=\"1\" time=\"0.004\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.004\">\n" +
            "        <skipped><![CDATA[" +
            "Given first step............................................................skipped\n" +
            "When second step............................................................skipped\n" +
            "Then third step.............................................................skipped\n" +
            "]]></skipped>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_handle_failure_in_before_hook_with_background() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Background: background name\n" +
                "    Given first step\n" +
                "  Scenario: scenario name\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("failed")));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"1\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0.004\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.004\">\n" +
            "        <failure message=\"the message\" type=\"io.cucumber.core.runner.TestHelper$1MockedTestAbortedException\"><![CDATA[" +
            "Given first step............................................................skipped\n" +
            "When second step............................................................skipped\n" +
            "Then third step.............................................................skipped\n" +
            "\n" +
            "StackTrace:\n" +
            "the stack trace" +
            "]]></failure>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_handle_failure_in_after_hook() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        hooks.add(TestHelper.hookEntry("after", result("failed")));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"1\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0.004\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.004\">\n" +
            "        <failure message=\"the message\" type=\"io.cucumber.core.runner.TestHelper$1MockedTestAbortedException\"><![CDATA[" +
            "Given first step............................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "\n" +
            "StackTrace:\n" +
            "the stack trace" +
            "]]></failure>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_accumulate_time_from_steps_and_hooks() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    * first step\n" +
                "    * second step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0.004\">\n" +
            "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.004\">\n" +
            "        <system-out><![CDATA[" +
            "* first step................................................................passed\n" +
            "* second step...............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_scenario_outlines() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario Outline: outline_name\n" +
                "    Given first step \"<arg>\"\n" +
                "    When second step\n" +
                "    Then third step\n\n" +
                "  Examples: examples\n" +
                "    | arg |\n" +
                "    |  a  |\n" +
                "    |  b  |\n");
        features.add(feature);
        stepsToResult.put("first step \"a\"", result("passed"));
        stepsToResult.put("first step \"b\"", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"2\" time=\"0.006\">\n" +
            "    <testcase classname=\"feature name\" name=\"outline_name\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"a\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "    <testcase classname=\"feature name\" name=\"outline_name_2\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"b\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_scenario_outlines_with_multiple_examples() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario Outline: outline name\n" +
                "    Given first step \"<arg>\"\n" +
                "    When second step\n" +
                "    Then third step\n\n" +
                "  Examples: examples 1\n" +
                "    | arg |\n" +
                "    |  a  |\n" +
                "    |  b  |\n\n" +
                "  Examples: examples 2\n" +
                "    | arg |\n" +
                "    |  c  |\n" +
                "    |  d  |\n");
        features.add(feature);
        stepsToResult.put("first step \"a\"", result("passed"));
        stepsToResult.put("first step \"b\"", result("passed"));
        stepsToResult.put("first step \"c\"", result("passed"));
        stepsToResult.put("first step \"d\"", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"4\" time=\"0.012\">\n" +
            "    <testcase classname=\"feature name\" name=\"outline name\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"a\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "    <testcase classname=\"feature name\" name=\"outline name 2\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"b\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "    <testcase classname=\"feature name\" name=\"outline name 3\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"c\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "    <testcase classname=\"feature name\" name=\"outline name 4\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"d\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    @Test
    void should_format_scenario_outlines_with_arguments_in_name() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario Outline: outline name <arg>\n" +
                "    Given first step \"<arg>\"\n" +
                "    When second step\n" +
                "    Then third step\n\n" +
                "  Examples: examples 1\n" +
                "    | arg |\n" +
                "    |  a  |\n" +
                "    |  b  |\n");
        features.add(feature);
        stepsToResult.put("first step \"a\"", result("passed"));
        stepsToResult.put("first step \"b\"", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        stepDuration = Duration.ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<testsuite failures=\"0\" name=\"io.cucumber.core.plugin.JUnitFormatter\" skipped=\"0\" errors=\"0\" tests=\"2\" time=\"0.006\">\n" +
            "    <testcase classname=\"feature name\" name=\"outline name a\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"a\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "    <testcase classname=\"feature name\" name=\"outline name b\" time=\"0.003\">\n" +
            "        <system-out><![CDATA[" +
            "Given first step \"b\"........................................................passed\n" +
            "When second step............................................................passed\n" +
            "Then third step.............................................................passed\n" +
            "]]></system-out>\n" +
            "    </testcase>\n" +
            "</testsuite>\n";
        assertXmlEqual(expected, formatterOutput);
    }

    private File runFeaturesWithJunitFormatter(final List<String> featurePaths) throws IOException {
        return runFeaturesWithJunitFormatter(featurePaths, false);
    }

    private File runFeaturesWithJunitFormatter(final List<String> featurePaths, boolean strict) throws IOException {
        File report = File.createTempFile("cucumber-jvm-junit", "xml");

        List<String> args = new ArrayList<>();
        if (strict) {
            args.add("--strict");
        }
        args.add("--plugin");
        args.add("junit:" + report.getAbsolutePath());
        args.addAll(featurePaths);

        TestHelper.builder()
            .withRuntimeArgs(args)
            .withFeatures(features)
            .withStepsToResult(stepsToResult)
            .withStepsToLocation(stepsToLocation)
            .withHooks(hooks)
            .withHookLocations(hookLocations)
            .withHookActions(hookActions)
            .withTimeServiceType(TestHelper.TimeServiceType.FIXED_INCREMENT)
            .withTimeServiceIncrement(ZERO)
            .build()
            .run();

        return report;
    }

    private String runFeaturesWithFormatter() throws IOException {
        final File report = File.createTempFile("cucumber-jvm-junit", ".xml");
        final JUnitFormatter formatter = createJUnitFormatter(report);
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

        Scanner scanner = new Scanner(new FileInputStream(report), "UTF-8");
        String formatterOutput = scanner.useDelimiter("\\A").next();
        scanner.close();
        return formatterOutput;
    }

    private JUnitFormatter createJUnitFormatter(final File report) throws IOException {
        return new JUnitFormatter(report.toURI().toURL());
    }

    private String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
