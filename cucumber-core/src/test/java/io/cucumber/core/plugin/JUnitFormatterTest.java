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
import org.opentest4j.TestAbortedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Clock.fixed;
import static java.time.Duration.ofMillis;
import static java.time.Instant.EPOCH;
import static java.time.ZoneId.of;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

class JUnitFormatterTest {

    private static void assertXmlEqual(String expected, ByteArrayOutputStream actual) {
        String actualString = new String(actual.toByteArray(), UTF_8);
        assertThat(actualString, isIdenticalTo(expected).ignoreWhitespace());
    }

    @Test
    void should_format_passed_scenario() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testsuite failures=\"0\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <system-out><![CDATA[" +
                "Given first step............................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";

        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Background:\n" +
                        "    Given first background step\n" +
                        "    When second background step\n" +
                        "    Then third background step\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first background step"),
                        new StubStepDefinition("second background step"),
                        new StubStepDefinition("third background step"),
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"Cucumber\" skipped=\"0\" tests=\"1\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <system-out>\n" +
                "            <![CDATA[Given first background step.................................................passed\n"
                +
                "When second background step.................................................passed\n" +
                "Then third background step..................................................passed\n" +
                "Given first step............................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]>\n" +
                "        </system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";

        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_multiple_scenarios() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: First scenario\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n" +
                        "  Scenario: Second scenario\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"Cucumber\" skipped=\"0\" tests=\"2\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"First scenario\" time=\"0\">\n" +
                "        <system-out>\n" +
                "            <![CDATA[Given first step............................................................passed\n"
                +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]>\n" +
                "        </system-out>\n" +
                "    </testcase>\n" +
                "    <testcase classname=\"feature name\" name=\"Second scenario\" time=\"0\">\n" +
                "        <system-out>\n" +
                "            <![CDATA[Given first step............................................................passed\n"
                +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]>\n" +
                "        </system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";

        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_empty_scenario_as_passing() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite failures=\"0\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_skipped_scenario() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        RuntimeException exception = new TestAbortedException("message");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step", exception),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();

        String stackTrace = getStackTrace(exception);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite failures=\"0\" name=\"Cucumber\" skipped=\"1\" errors=\"0\" tests=\"1\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <skipped>" +
                "<![CDATA["
                + stackTrace +
                "]]></skipped>\n" +
                "       <system-out>\n" +
                "           <![CDATA[" +
                "Given first step............................................................skipped\n" +
                "When second step............................................................skipped\n" +
                "Then third step.............................................................skipped\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    private String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Test
    void should_format_pending_scenario() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step", new StubPendingException()),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite errors=\"0\" failures=\"1\" name=\"Cucumber\" skipped=\"0\" tests=\"1\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <failure>\n" +
                "           <![CDATA[TODO: implement me]]>\n" +
                "       </failure>\n"
                +
                "       <system-out>" +
                "            <![CDATA[Given first step............................................................pending\n"
                +
                "When second step............................................................skipped\n" +
                "Then third step.............................................................skipped\n" +
                "]]>\n" +
                "        </system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_failed_scenario() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step", new StubException("the message", "the stack trace"))))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testsuite failures=\"1\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n"+
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "  <failure><![CDATA[the stack trace]]></failure>\n" +
                "  <system-out><![CDATA[Given first step............................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................failed]]></system-out>\n" +
                "</testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_handle_failure_in_before_hook() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        singletonList(new StubHookDefinition(new StubException("the message", "the stack trace"))),
                        Arrays.asList(
                                new StubStepDefinition("first step"),
                                new StubStepDefinition("second step"),
                                new StubStepDefinition("third step")),
                        singletonList(new StubHookDefinition())))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite failures=\"1\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <failure><![CDATA[the stack trace]]>\n"
                +
                "       </failure>\n" +
                "       <system-out>" +
                "           <![CDATA[" +
                "Given first step............................................................skipped\n" +
                "When second step............................................................skipped\n" +
                "Then third step.............................................................skipped\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_handle_pending_in_before_hook() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        singletonList(new StubHookDefinition(new StubPendingException())),
                        Arrays.asList(
                                new StubStepDefinition("first step"),
                                new StubStepDefinition("second step"),
                                new StubStepDefinition("third step")),
                        singletonList(new StubHookDefinition())))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<testsuite failures=\"1\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n" +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <failure><![CDATA[TODO: implement me]]></failure>\n" +
                "        <system-out><![CDATA[Given first step............................................................skipped\n" +
                "When second step............................................................skipped\n" +
                "Then third step.............................................................skipped]]>" +
                "        </system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_handle_failure_in_before_hook_with_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Background: background name\n" +
                        "    Given first step\n" +
                        "  Scenario: scenario name\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        singletonList(new StubHookDefinition(new StubException("the message", "the stack trace"))),
                        Arrays.asList(
                                new StubStepDefinition("first step"),
                                new StubStepDefinition("second step"),
                                new StubStepDefinition("third step")),
                        singletonList(new StubHookDefinition())))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite failures=\"1\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"1\" time=\"0\">\n" +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <failure>\n" +
                "           <![CDATA[the stack trace]]>\n" +
                "       </failure>\n" +
                "       <system-out>\n" +
                "           <![CDATA[" +
                "Given first step............................................................skipped\n" +
                "When second step............................................................skipped\n" +
                "Then third step.............................................................skipped\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_handle_failure_in_after_hook() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        singletonList(new StubHookDefinition()),
                        Arrays.asList(
                                new StubStepDefinition("first step"),
                                new StubStepDefinition("second step"),
                                new StubStepDefinition("third step")),
                        singletonList(new StubHookDefinition(new StubException("the message", "the stack trace")))))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite name=\"Cucumber\" time=\"0\" tests=\"1\" skipped=\"0\" failures=\"1\" errors=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0\">\n" +
                "        <failure>\n" +
                "           <![CDATA[the stack trace]]>\n" +
                "       </failure>\n" +
                "       <system-out>\n" +
                "       <![CDATA[Given first step............................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_accumulate_time_from_steps_and_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    * first step\n" +
                        "    * second step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        singletonList(new StubHookDefinition()),
                        Arrays.asList(
                                new StubStepDefinition("first step"),
                                new StubStepDefinition("second step"),
                                new StubStepDefinition("third step")),
                        singletonList(new StubHookDefinition())))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite name=\"Cucumber\" time=\"0.004\" tests=\"1\" skipped=\"0\" failures=\"0\" errors=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"scenario name\" time=\"0.004\">\n" +
                "        <system-out><![CDATA[" +
                "* first step................................................................passed\n" +
                "* second step...............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_scenario_outlines() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario Outline: outline_name\n" +
                        "    Given first step \"<arg>\"\n" +
                        "    When second step\n" +
                        "    Then third step\n\n" +
                        "  Examples: examples\n" +
                        "    | arg |\n" +
                        "    |  a  |\n" +
                        "    |  b  |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step {string}", String.class),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite name=\"Cucumber\" time=\"0\" tests=\"2\" skipped=\"0\" failures=\"0\" errors=\"0\">\n"
                +
                "    <testcase classname=\"feature name\" name=\"outline_name - examples - Example #1.1\" time=\"0\">\n" +
                "        <system-out><![CDATA[" +
                "Given first step \"a\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "    <testcase classname=\"feature name\" name=\"outline_name - examples - Example #1.2\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"b\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_scenario_outlines_with_multiple_examples() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step {string}", String.class),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite name=\"Cucumber\" time=\"0\" tests=\"4\" skipped=\"0\" failures=\"0\" errors=\"0\">\n" +
                "    <testcase classname=\"feature name\" name=\"outline name - examples 1 - Example #1.1\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"a\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "    <testcase classname=\"feature name\" name=\"outline name - examples 1 - Example #1.2\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"b\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "    <testcase classname=\"feature name\" name=\"outline name - examples 2 - Example #2.1\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"c\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "    <testcase classname=\"feature name\" name=\"outline name - examples 2 - Example #2.2\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"d\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

    @Test
    void should_format_scenario_outlines_with_arguments_in_name() {
        Feature feature = TestFeatureParser.parse("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario Outline: outline name <arg>\n" +
                        "    Given first step \"<arg>\"\n" +
                        "    When second step\n" +
                        "    Then third step\n\n" +
                        "  Examples: examples 1\n" +
                        "    | arg |\n" +
                        "    |  a  |\n" +
                        "    |  b  |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JUnitFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step {string}", String.class),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")))
                .build()
                .run();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite failures=\"0\" name=\"Cucumber\" skipped=\"0\" errors=\"0\" tests=\"2\" time=\"0\">\n" +
                "    <testcase classname=\"feature name\" name=\"outline name &lt;arg&gt; - examples 1 - Example #1.1\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"a\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "    <testcase classname=\"feature name\" name=\"outline name &lt;arg&gt; - examples 1 - Example #1.2\" time=\"0\">" +
                "        <system-out><![CDATA[" +
                "Given first step \"b\"........................................................passed\n" +
                "When second step............................................................passed\n" +
                "Then third step.............................................................passed\n" +
                "]]></system-out>\n" +
                "    </testcase>\n" +
                "</testsuite>\n";
        assertXmlEqual(expected, out);
    }

}
