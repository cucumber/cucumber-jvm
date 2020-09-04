package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubPendingException;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Clock.fixed;
import static java.time.Duration.ofMillis;
import static java.time.Instant.EPOCH;
import static java.time.ZoneId.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

final class TestNGFormatterTest {

    private static void assertXmlEquals(String expected, ByteArrayOutputStream actual) {
        String actualString = new String(actual.toByteArray(), UTF_8);
        assertThat(actualString, isIdenticalTo(expected).ignoreWhitespace());
    }

    @Test
    void testScenarioWithUndefinedSteps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testng-results failed=\"1\" passed=\"0\" skipped=\"0\" total=\"1\">\n" +
                "    <suite duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "        <test duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "            <class name=\"feature\">\n" +
                "                <test-method duration-ms=\"0\" finished-at=\"1970-01-01T00:00:00Z\" name=\"scenario\" started-at=\"1970-01-01T00:00:00Z\" status=\"FAIL\">\n"
                +
                "                    <exception class=\"The scenario has pending or undefined step(s)\">\n" +
                "                        <message>\n" +
                "                            <![CDATA[When step...................................................................undefined\n"
                +
                "Then step...................................................................skipped\n" +
                "]]>\n" +
                "                        </message>\n" +
                "                        <full-stacktrace>\n" +
                "                            <![CDATA[The scenario has pending or undefined step(s)]]>\n" +
                "                        </full-stacktrace>\n" +
                "                    </exception>\n" +
                "                </test-method>\n" +
                "            </class>\n" +
                "        </test>\n" +
                "    </suite>\n" +
                "</testng-results>\n";

        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioWithPendingSteps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step1\n" +
                "    Then step2\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("step1", new StubPendingException()),
                    new StubStepDefinition("step2")))
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testng-results failed=\"1\" passed=\"0\" skipped=\"0\" total=\"1\">\n" +
                "    <suite duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "        <test duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "            <class name=\"feature\">\n" +
                "                <test-method duration-ms=\"0\" finished-at=\"1970-01-01T00:00:00Z\" name=\"scenario\" started-at=\"1970-01-01T00:00:00Z\" status=\"FAIL\">\n"
                +
                "                    <exception class=\"The scenario has pending or undefined step(s)\">\n" +
                "                        <message>\n" +
                "                            <![CDATA[When step1..................................................................pending\n"
                +
                "Then step2..................................................................skipped\n" +
                "]]>\n" +
                "                        </message>\n" +
                "                        <full-stacktrace>\n" +
                "                            <![CDATA[The scenario has pending or undefined step(s)]]>\n" +
                "                        </full-stacktrace>\n" +
                "                    </exception>\n" +
                "                </test-method>\n" +
                "            </class>\n" +
                "        </test>\n" +
                "    </suite>\n" +
                "</testng-results>\n";
        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioWithFailedSteps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step1\n" +
                "    Then step2\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("step1", new StubException("message", "stacktrace")),
                    new StubStepDefinition("step2")))
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">"
                +
                "                    <exception class=\"io.cucumber.core.plugin.StubException\">" +
                "                        <message><![CDATA[When step1..................................................................failed\n"
                +
                "Then step2..................................................................skipped\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[stacktrace]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>";
        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioWithPassedSteps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("step")))
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"1\" failed=\"0\" skipped=\"0\">" +
                "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"PASS\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\"/>"
                +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>";
        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioWithBackground() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Background:\n" +
                "    When background\n" +
                "    Then background\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testng-results failed=\"1\" passed=\"0\" skipped=\"0\" total=\"1\">\n" +
                "    <suite duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "        <test duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "            <class name=\"feature\">\n" +
                "                <test-method duration-ms=\"0\" finished-at=\"1970-01-01T00:00:00Z\" name=\"scenario\" started-at=\"1970-01-01T00:00:00Z\" status=\"FAIL\">\n"
                +
                "                    <exception class=\"The scenario has pending or undefined step(s)\">\n" +
                "                        <message>\n" +
                "                            <![CDATA[When background.............................................................undefined\n"
                +
                "Then background.............................................................skipped\n" +
                "When step...................................................................skipped\n" +
                "Then step...................................................................skipped\n" +
                "]]>\n" +
                "                        </message>\n" +
                "                        <full-stacktrace>\n" +
                "                            <![CDATA[The scenario has pending or undefined step(s)]]>\n" +
                "                        </full-stacktrace>\n" +
                "                    </exception>\n" +
                "                </test-method>\n" +
                "            </class>\n" +
                "        </test>\n" +
                "    </suite>\n" +
                "</testng-results>\n";

        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioOutlineWithExamples() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario Outline: scenario\n" +
                "    When step\n" +
                "    Then step\n" +
                "    Examples:\n" +
                "    | arg |\n" +
                "    |  1  |\n" +
                "    |  2  |\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testng-results failed=\"2\" passed=\"0\" skipped=\"0\" total=\"2\">\n" +
                "    <suite duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "        <test duration-ms=\"0\" name=\"io.cucumber.core.plugin.TestNGFormatter\">\n" +
                "            <class name=\"feature\">\n" +
                "                <test-method duration-ms=\"0\" finished-at=\"1970-01-01T00:00:00Z\" name=\"scenario\" started-at=\"1970-01-01T00:00:00Z\" status=\"FAIL\">\n"
                +
                "                    <exception class=\"The scenario has pending or undefined step(s)\">\n" +
                "                        <message>\n" +
                "                            <![CDATA[When step...................................................................undefined\n"
                +
                "Then step...................................................................skipped\n" +
                "]]>\n" +
                "                        </message>\n" +
                "                        <full-stacktrace>\n" +
                "                            <![CDATA[The scenario has pending or undefined step(s)]]>\n" +
                "                        </full-stacktrace>\n" +
                "                    </exception>\n" +
                "                </test-method>\n" +
                "                <test-method duration-ms=\"0\" finished-at=\"1970-01-01T00:00:00Z\" name=\"scenario_2\" started-at=\"1970-01-01T00:00:00Z\" status=\"FAIL\">\n"
                +
                "                    <exception class=\"The scenario has pending or undefined step(s)\">\n" +
                "                        <message>\n" +
                "                            <![CDATA[When step...................................................................undefined\n"
                +
                "Then step...................................................................skipped\n" +
                "]]>\n" +
                "                        </message>\n" +
                "                        <full-stacktrace>\n" +
                "                            <![CDATA[The scenario has pending or undefined step(s)]]>\n" +
                "                        </full-stacktrace>\n" +
                "                    </exception>\n" +
                "                </test-method>\n" +
                "            </class>\n" +
                "        </test>\n" +
                "    </suite>\n" +
                "</testng-results>\n";
        assertXmlEquals(expected, out);
    }

    @Test
    void testDurationCalculationOfStepsAndHooks() {
        Feature feature1 = TestFeatureParser.parse("path/feature1.feature", "" +
                "Feature: feature_1\n" +
                "  Scenario: scenario_1\n" +
                "    When step\n" +
                "    Then step\n" +
                "  Scenario: scenario_2\n" +
                "    When step\n" +
                "    Then step\n");
        Feature feature2 = TestFeatureParser.parse("path/feature2.feature", "" +
                "Feature: feature_2\n" +
                "  Scenario: scenario_3\n" +
                "    When step\n" +
                "    Then step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(() -> Arrays.asList(feature1, feature2))
                .withAdditionalPlugins(timeService, new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition()),
                    singletonList(new StubStepDefinition("step")),
                    singletonList(new StubHookDefinition())))
                .build()
                .run();
        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"3\" passed=\"3\" failed=\"0\" skipped=\"0\">" +
                "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"12\">" +
                "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"12\">" +
                "            <class name=\"feature_1\">" +
                "                <test-method name=\"scenario_1\" status=\"PASS\" duration-ms=\"4\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00.004Z\"/>"
                +
                "                <test-method name=\"scenario_2\" status=\"PASS\" duration-ms=\"4\" started-at=\"1970-01-01T00:00:00.004Z\" finished-at=\"1970-01-01T00:00:00.008Z\"/>"
                +
                "            </class>" +
                "            <class name=\"feature_2\">" +
                "                <test-method name=\"scenario_3\" status=\"PASS\" duration-ms=\"4\" started-at=\"1970-01-01T00:00:00.008Z\" finished-at=\"1970-01-01T00:00:00.012Z\"/>"
                +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>";
        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioWithFailedBeforeHook() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(new StubException("message", "stacktrace"))),
                    singletonList(new StubStepDefinition("step")),
                    emptyList()))
                .build()
                .run();

        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">"
                +
                "                    <exception class=\"io.cucumber.core.plugin.StubException\">" +
                "                        <message><![CDATA[When step...................................................................skipped\n"
                +
                "Then step...................................................................skipped\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[stacktrace]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>";
        assertXmlEquals(expected, out);
    }

    @Test
    void testScenarioWithFailedAfterHook() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature\n" +
                "  Scenario: scenario\n" +
                "    When step\n" +
                "    Then step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TestNGFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    singletonList(new StubStepDefinition("step")),
                    singletonList(new StubHookDefinition(new StubException("message", "stacktrace")))))
                .build()
                .run();
        String expected = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testng-results total=\"1\" passed=\"0\" failed=\"1\" skipped=\"0\">" +
                "    <suite name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "        <test name=\"io.cucumber.core.plugin.TestNGFormatter\" duration-ms=\"0\">" +
                "            <class name=\"feature\">" +
                "                <test-method name=\"scenario\" status=\"FAIL\" duration-ms=\"0\" started-at=\"1970-01-01T00:00:00Z\" finished-at=\"1970-01-01T00:00:00Z\">"
                +
                "                    <exception class=\"io.cucumber.core.plugin.StubException\">" +
                "                        <message><![CDATA[When step...................................................................passed\n"
                +
                "Then step...................................................................passed\n" +
                "]]></message>" +
                "                        <full-stacktrace><![CDATA[stacktrace]]></full-stacktrace>" +
                "                    </exception>" +
                "                </test-method>" +
                "            </class>" +
                "        </test>" +
                "    </suite>" +
                "</testng-results>";
        assertXmlEquals(expected, out);
    }

}
