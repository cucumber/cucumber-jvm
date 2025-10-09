package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class UsageFormatterTest {

    @Test
    void writes_empty_report() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        String expected = "" +
                "{\n" +
                " \"stepDefinitions\": []\n" +
                "}";
        assertJsonEquals(expected, out);
    }

    @Test
    void writes_unused_report() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference())))
                .build()
                .run();

        String expected = "" +
                "{\n" +
                "  \"stepDefinitions\": [\n" +
                "    {\n" +
                "      \"expression\": \"first step\",\n" +
                "      \"location\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\",\n" +
                "      \"duration\": {\n" +
                "        \"sum\": 1.000000000,\n" +
                "        \"mean\": 1.000000000,\n" +
                "        \"moe\": 0.000000000\n" +
                "      },\n" +
                "      \"steps\": [\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 1.000000000,\n" +
                "          \"location\": \"path/test.feature:2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"expression\": \"second step\",\n" +
                "      \"location\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\",\n" +
                "      \"steps\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonEquals(expected, out);
    }

    @Test
    void writes_usage_report() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference())))
                .build()
                .run();

        String expected = "" +
                "{" +
                "  \"stepDefinitions\": [\n" +
                "    {\n" +
                "      \"expression\": \"first step\",\n" +
                "      \"location\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\",\n" +
                "      \"duration\": {\n" +
                "        \"sum\": 1.000000000,\n" +
                "        \"mean\": 1.000000000,\n" +
                "        \"moe\": 0.000000000\n" +
                "      },\n" +
                "      \"steps\": [\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 1.000000000,\n" +
                "          \"location\": \"path/test.feature:2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonEquals(expected, out);
    }

    @Test
    void writes_usage_with_standard_deviation() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario 1\n" +
                "    Given first step\n" +
                "  Scenario: scenario 2\n" +
                "    Given first step\n" +
                "  Scenario: scenario 3\n" +
                "    Given first step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(
            Duration.ofMillis(1000),
            Duration.ofMillis(2000),
            Duration.ofMillis(4000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference())))
                .build()
                .run();

        String expected = "" +
                "{\n" +
                "  \"stepDefinitions\": [\n" +
                "    {\n" +
                "      \"expression\": \"first step\",\n" +
                "      \"location\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\",\n" +
                "      \"duration\": {\n" +
                "        \"sum\": 7.000000000,\n" +
                "        \"mean\": 2.333333333,\n" +
                "        \"moe\": 1.440164599\n" +
                "      },\n" +
                "      \"steps\": [\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 1.000000000,\n" +
                "          \"location\": \"path/test.feature:2\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 2.000000000,\n" +
                "          \"location\": \"path/test.feature:4\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 4.000000000,\n" +
                "          \"location\": \"path/test.feature:6\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonEquals(expected, out);
    }

    @Test
    void writes_usage_with_standard_deviation__two_samples() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario 1\n" +
                "    Given first step\n" +
                "  Scenario: scenario 2\n" +
                "    Given first step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(
            Duration.ofMillis(2000),
            Duration.ofMillis(3000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference())))
                .build()
                .run();

        String expected = "" +
                "{\n" +
                "  \"stepDefinitions\": [\n" +
                "    {\n" +
                "      \"expression\": \"first step\",\n" +
                "      \"location\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\",\n" +
                "      \"duration\": {\n" +
                "        \"sum\": 5.000000000,\n" +
                "        \"mean\": 2.500000000,\n" +
                "        \"moe\": 0.707106781\n" +
                "      },\n" +
                "      \"steps\": [\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 2.000000000,\n" +
                "          \"location\": \"path/test.feature:2\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": 3.000000000,\n" +
                "          \"location\": \"path/test.feature:4\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonEquals(expected, out);
    }

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) throws JSONException {
        assertEquals(expected, new String(actual.toByteArray(), UTF_8), true);
    }

}
