package io.cucumber.core.plugin;

import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMillis;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonFormatterTest {

    final SourceReference thereAreBananas = getMethod("there_are_bananas");

    private static SourceReference getMethod(String name) {
        try {
            return SourceReference.fromMethod(JsonFormatterTestStepDefinitions.class.getMethod(name));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) throws JSONException {
        assertEquals(expected, new String(actual.toByteArray(), UTF_8), true);
    }

    @Test
    void should_format_scenario_with_a_passed_step() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, new IncrementingUuidGenerator()))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", thereAreBananas)))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"file:path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"name\": \"Monkey eats bananas\",\n" +
                "        \"line\": 3,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        assertJsonEquals(expected, out);
    }
}
