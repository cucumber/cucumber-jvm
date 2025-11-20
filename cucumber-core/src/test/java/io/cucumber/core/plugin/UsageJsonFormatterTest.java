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
import java.io.File;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class UsageJsonFormatterTest {

    @Test
    void writes_empty_report() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageJsonFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        assertJsonEquals("{\"stepDefinitions\":[]}", out);
    }

    @Test
    void writes_usage_report() throws JSONException {
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
                .withAdditionalPlugins(timeService, new UsageJsonFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference())))
                .build()
                .run();

        String featureFile = new File("").toPath().toUri() + "path/test.feature";
        String expected = "{\n" +
                "  \"stepDefinitions\": [\n" +
                "    {\n" +
                "      \"sourceReference\": {\n" +
                "        \"javaMethod\": {\n" +
                "          \"className\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition\",\n" +
                "          \"methodName\": \"one\",\n" +
                "          \"methodParameterTypes\": []\n" +
                "        }\n" +
                "      },\n" +
                "      \"duration\": {\n" +
                "        \"sum\": {\n" +
                "          \"seconds\": 7,\n" +
                "          \"nanos\": 0\n" +
                "        },\n" +
                "        \"mean\": {\n" +
                "          \"seconds\": 2,\n" +
                "          \"nanos\": 333333333\n" +
                "        },\n" +
                "        \"moe95\": {\n" +
                "          \"seconds\": 1,\n" +
                "          \"nanos\": 440164599\n" +
                "        }\n" +
                "      },\n" +
                "      \"matches\": [\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": {\n" +
                "            \"seconds\": 1,\n" +
                "            \"nanos\": 0\n" +
                "          },\n" +
                "          \"uri\": \"path/test.feature\",\n" +
                "          \"location\": {\n" +
                "            \"line\": 2,\n" +
                "            \"column\": 3\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": {\n" +
                "            \"seconds\": 2,\n" +
                "            \"nanos\": 0\n" +
                "          },\n" +
                "          \"uri\": \"path/test.feature\",\n" +
                "          \"location\": {\n" +
                "            \"line\": 4,\n" +
                "            \"column\": 3\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"text\": \"first step\",\n" +
                "          \"duration\": {\n" +
                "            \"seconds\": 4,\n" +
                "            \"nanos\": 0\n" +
                "          },\n" +
                "          \"uri\": \"path/test.feature\",\n" +
                "          \"location\": {\n" +
                "            \"line\": 6,\n" +
                "            \"column\": 3\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"expression\": {\n" +
                "        \"source\": \"first step\",\n" +
                "        \"type\": \"CUCUMBER_EXPRESSION\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"sourceReference\": {\n" +
                "        \"javaMethod\": {\n" +
                "          \"className\": \"io.cucumber.core.plugin.PrettyFormatterStepDefinition\",\n" +
                "          \"methodName\": \"two\",\n" +
                "          \"methodParameterTypes\": []\n" +
                "        }\n" +
                "      },\n" +
                "      \"matches\": [],\n" +
                "      \"expression\": {\n" +
                "        \"source\": \"second step\",\n" +
                "        \"type\": \"CUCUMBER_EXPRESSION\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonEquals(expected.replaceAll("path/test.feature", featureFile), out);
    }

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) throws JSONException {
        assertEquals(expected, new String(actual.toByteArray(), UTF_8), true);
    }

}
