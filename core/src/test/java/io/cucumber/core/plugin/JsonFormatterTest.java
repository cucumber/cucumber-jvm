package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.Runtime.Builder;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.datatable.DataTable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Clock.fixed;
import static java.time.Duration.ofMillis;
import static java.time.Instant.EPOCH;
import static java.time.ZoneId.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

class JsonFormatterTest {

    @Test
    void featureWithOutlineTest() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        createRuntime(out)
                .build()
                .run();

        InputStream resourceAsStream = getClass().getResourceAsStream("JsonPrettyFormatterTest.json");
        String expected = new Scanner(resourceAsStream, "UTF-8")
                .useDelimiter("\\A")
                .next();

        assertJsonEquals(expected, out);
    }

    private Builder createRuntime(ByteArrayOutputStream out) {
        Feature feature = TestFeatureParser.parse(
            "classpath:io/cucumber/core/plugin/JsonPrettyFormatterTest.feature",
            getClass().getResourceAsStream("JsonPrettyFormatterTest.feature"));

        return Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition()),
                    asList(
                        new StubStepDefinition("bg_1"),
                        new StubStepDefinition("bg_2"),
                        new StubStepDefinition("bg_3"),
                        new StubStepDefinition("step_1"),
                        new StubStepDefinition("step_2"),
                        new StubStepDefinition("step_3"),
                        new StubStepDefinition("cliché"),
                        new StubStepDefinition("so_1 {int}", Integer.class),
                        new StubStepDefinition("so_2 {int} cucumbers", Integer.class),
                        new StubStepDefinition("{int} so_3", Integer.class),
                        new StubStepDefinition("a"),
                        new StubStepDefinition("b"),
                        new StubStepDefinition("c")),
                    emptyList()))
                .withAdditionalPlugins(new JsonFormatter(out));
    }

    @Test
    void featureWithOutlineTestParallel() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        createRuntime(out)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setThreads(2).build())
                .build()
                .run();

        InputStream resourceAsStream = getClass().getResourceAsStream("JsonPrettyFormatterTest.json");
        String expected = new Scanner(resourceAsStream, "UTF-8")
                .useDelimiter("\\A")
                .next();

        assertJsonEquals(expected, out);
    }

    @Test
    void should_format_scenario_with_an_undefined_step() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier())
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
                "            \"match\": {},\n" +
                "            \"result\": {\n" +
                "              \"status\": \"undefined\"\n" +
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

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) {
        assertJsonEquals(expected, new String(actual.toByteArray(), UTF_8));

    }

    private void assertJsonEquals(String expected, String actual) {
        assertThat(actual, sameJSONAs(expected));
    }

    @Test
    void should_format_scenario_with_a_passed_step() {
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
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")))
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
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_format_scenario_with_a_failed_step() {
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
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()",
                        new StubException())))
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
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"failed\",\n" +
                "              \"error_message\": \"the stack trace\",\n" +
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

    @Test
    void should_format_scenario_with_a_rule() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Rule: This is all monkey business\n" +
                "    Scenario: Monkey eats bananas\n" +
                "      Given there are bananas\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"line\": 1,\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"line\": 4,\n" +
                "        \"name\": \"Monkey eats bananas\",\n" +
                "        \"description\": \"\",\n" +
                "        \"id\": \";monkey-eats-bananas\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"line\": 5,\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"keyword\": \"Given \"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"description\": \"\",\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"uri\": \"file:path/test.feature\",\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        assertJsonEquals(expected, out);
    }

    @Test
    void should_format_scenario_with_a_rule_and_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Background: \n" +
                "     Given there are bananas\n" +
                "\n" +
                "  Rule: This is all monkey business\n" +
                "\n" +
                "  Background: \n" +
                "     Given there are bananas\n" +
                "\n" +
                "    Scenario: Monkey eats bananas\n" +
                "      Given there are bananas\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"line\": 1,\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"line\": 3,\n" +
                "        \"name\": \"\",\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"background\",\n" +
                "        \"keyword\": \"Background\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"line\": 4,\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"keyword\": \"Given \"\n" +
                "          },\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"line\": 9,\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"keyword\": \"Given \"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"line\": 11,\n" +
                "        \"name\": \"Monkey eats bananas\",\n" +
                "        \"description\": \"\",\n" +
                "        \"id\": \";monkey-eats-bananas\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"line\": 12,\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"keyword\": \"Given \"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"description\": \"\",\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"uri\": \"file:path/test.feature\",\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        assertJsonEquals(expected, out);
    }

    @Test
    void should_format_scenario_outline_with_one_example() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Fruit party\n" +
                "\n" +
                "  Scenario Outline: Monkey eats fruits\n" +
                "    Given there are <fruits>\n" +
                "      Examples: Fruit table\n" +
                "      | fruits  |\n" +
                "      | bananas |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"fruit-party\",\n" +
                "    \"uri\": \"file:path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Fruit party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"fruit-party;monkey-eats-fruits;fruit-table;2\",\n" +
                "        \"keyword\": \"Scenario Outline\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"name\": \"Monkey eats fruits\",\n" +
                "        \"line\": 7,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_format_feature_with_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Background: There are bananas\n" +
                "    Given there are bananas\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Then the monkey eats bananas\n" +
                "\n" +
                "  Scenario: Monkey eats more bananas\n" +
                "    Then the monkey eats more bananas\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()"),
                    new StubStepDefinition("the monkey eats bananas", "StepDefs.monkey_eats_bananas()"),
                    new StubStepDefinition("the monkey eats more bananas", "StepDefs.monkey_eats_more_bananas()")))
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
                "        \"keyword\": \"Background\",\n" +
                "        \"name\": \"There are bananas\",\n" +
                "        \"line\": 3,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"background\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"name\": \"Monkey eats bananas\",\n" +
                "        \"line\": 6,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Then \",\n" +
                "            \"name\": \"the monkey eats bananas\",\n" +
                "            \"line\": 7,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.monkey_eats_bananas()\"\n" +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"keyword\": \"Background\",\n" +
                "        \"name\": \"There are bananas\",\n" +
                "        \"line\": 3,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"background\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-more-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.002Z\",\n" +
                "        \"name\": \"Monkey eats more bananas\",\n" +
                "        \"line\": 9,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Then \",\n" +
                "            \"name\": \"the monkey eats more bananas\",\n" +
                "            \"line\": 10,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.monkey_eats_more_bananas()\"\n" +
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

    @Test
    void should_format_feature_and_scenario_with_tags() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "@Party @Banana\n" +
                "Feature: Banana party\n" +
                "  @Monkey\n" +
                "  Scenario: Monkey eats more bananas\n" +
                "    Then the monkey eats more bananas\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("the monkey eats more bananas", "StepDefs.monkey_eats_more_bananas()")))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"line\": 2,\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"line\": 4,\n" +
                "        \"name\": \"Monkey eats more bananas\",\n" +
                "        \"description\": \"\",\n" +
                "        \"id\": \"banana-party;monkey-eats-more-bananas\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"line\": 5,\n" +
                "            \"name\": \"the monkey eats more bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.monkey_eats_more_bananas()\"\n" +
                "            },\n" +
                "            \"keyword\": \"Then \"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"tags\": [\n" +
                "          {\n" +
                "            \"name\": \"@Party\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"@Banana\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"@Monkey\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"description\": \"\",\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"uri\": \"file:path/test.feature\",\n" +
                "    \"tags\": [\n" +
                "      {\n" +
                "        \"name\": \"@Party\",\n" +
                "        \"type\": \"Tag\",\n" +
                "        \"location\": {\n" +
                "          \"line\": 1,\n" +
                "          \"column\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"@Banana\",\n" +
                "        \"type\": \"Tag\",\n" +
                "        \"location\": {\n" +
                "          \"line\": 1,\n" +
                "          \"column\": 8\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";
        assertJsonEquals(expected, out);
    }

    @Test
    void should_format_scenario_with_hooks() {
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
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition("Hooks.before_hook_1()")),
                    singletonList(new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")),
                    singletonList(new StubHookDefinition("Hooks.after_hook_1()"))))
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
                "        \"before\": [\n" +
                "          {\n" +
                "            \"match\": {\n" +
                "              \"location\": \"Hooks.before_hook_1()\"\n" +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"after\": [\n" +
                "          {\n" +
                "            \"match\": {\n" +
                "              \"location\": \"Hooks.after_hook_1()\"\n" +
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

    @Test
    void should_add_step_hooks_to_step() {
        Feature feature = TestFeatureParser.parse("file:path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "    When monkey arrives\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    singletonList(new StubHookDefinition("Hooks.beforestep_hooks_1()")),
                    asList(
                        new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()"),
                        new StubStepDefinition("monkey arrives", "StepDefs.monkey_arrives()")),
                    asList(
                        new StubHookDefinition("Hooks.afterstep_hooks_1()"),
                        new StubHookDefinition("Hooks.afterstep_hooks_2()")),
                    emptyList()))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"line\": 1,\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"line\": 3,\n" +
                "        \"name\": \"Monkey eats bananas\",\n" +
                "        \"description\": \"\",\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"before\": [\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"Hooks.beforestep_hooks_1()\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"line\": 4,\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
                "            },\n" +
                "            \"after\": [\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"Hooks.afterstep_hooks_2()\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"Hooks.afterstep_hooks_1()\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"keyword\": \"Given \"\n" +
                "          },\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"duration\": 1000000,\n" +
                "              \"status\": \"passed\"\n" +
                "            },\n" +
                "            \"before\": [\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"Hooks.beforestep_hooks_1()\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"line\": 5,\n" +
                "            \"name\": \"monkey arrives\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.monkey_arrives()\"\n" +
                "            },\n" +
                "            \"after\": [\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"Hooks.afterstep_hooks_2()\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"Hooks.afterstep_hooks_1()\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"keyword\": \"When \"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"description\": \"\",\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"uri\": \"file:path/test.feature\",\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        assertJsonEquals(expected, out);
    }

    @Test
    void should_handle_write_from_a_hook() {
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
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition("Hooks.before_hook_1()",
                        testCaseState -> testCaseState.log("printed from hook"))),
                    singletonList(new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")),
                    emptyList()))
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
                "        \"before\": [\n" +
                "          {\n" +
                "            \"match\": {\n" +
                "              \"location\": \"Hooks.before_hook_1()\"\n" +
                "            },\n" +
                "            \"output\": [\n" +
                "              \"printed from hook\"\n" +
                "            ],\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_handle_embed_from_a_hook() {
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
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition("Hooks.before_hook_1()",
                        testCaseState -> testCaseState
                                .attach(new byte[] { 1, 2, 3 }, "mime-type;base64", null))),
                    singletonList(new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")),
                    emptyList()))
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
                "        \"before\": [\n" +
                "          {\n" +
                "            \"match\": {\n" +
                "              \"location\": \"Hooks.before_hook_1()\"\n" +
                "            },\n" +
                "            \"embeddings\": [\n" +
                "              {\n" +
                "                \"mime_type\": \"mime-type;base64\",\n" +
                "                \"data\": \"AQID\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_handle_embed_with_name_from_a_hook() {
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
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition("Hooks.before_hook_1()",
                        testCaseState -> testCaseState.attach(new byte[] { 1, 2, 3 }, "mime-type;base64",
                            "someEmbedding"))),
                    singletonList(new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()")),
                    emptyList()))
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
                "        \"before\": [\n" +
                "          {\n" +
                "            \"match\": {\n" +
                "              \"location\": \"Hooks.before_hook_1()\"\n" +
                "            },\n" +
                "            \"embeddings\": [\n" +
                "              {\n" +
                "                \"mime_type\": \"mime-type;base64\",\n" +
                "                \"data\": \"AQID\",\n" +
                "                \"name\": \"someEmbedding\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"result\": {\n" +
                "              \"status\": \"passed\",\n" +
                "              \"duration\": 1000000\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_format_scenario_with_a_step_with_a_doc_string() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "    \"\"\"\n" +
                "    doc string content\n" +
                "    \"\"\"\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()", String.class)))
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
                "            \"doc_string\": {\n" +
                "              \"value\": \"doc string content\",\n" +
                "              \"line\": 5\n" +
                "            },\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_format_scenario_with_a_step_with_a_doc_string_and_content_type() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "    \"\"\"doc\n" +
                "    doc string content\n" +
                "    \"\"\"\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()", String.class)))
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
                "            \"doc_string\": {\n" +
                "              \"content_type\": \"doc\",\n" +
                "              \"value\": \"doc string content\",\n" +
                "              \"line\": 5\n" +
                "            },\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_format_scenario_with_a_step_with_a_data_table() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "      | aa | 11 |\n" +
                "      | bb | 22 |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()", DataTable.class)))
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
                "            \"rows\": [\n" +
                "              {\n" +
                "                \"cells\": [\n" +
                "                  \"aa\",\n" +
                "                  \"11\"\n" +
                "                ]\n" +
                "              },\n" +
                "              {\n" +
                "                \"cells\": [\n" +
                "                  \"bb\",\n" +
                "                  \"22\"\n" +
                "                ]\n" +
                "              }\n" +
                "            ],\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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

    @Test
    void should_handle_several_features() {
        Feature feature1 = TestFeatureParser.parse("path/test1.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Feature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
                "Feature: Orange party\n" +
                "\n" +
                "  Scenario: Monkey eats oranges\n" +
                "    Given there are oranges\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature1, feature2))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", "StepDefs.there_are_bananas()"),
                    new StubStepDefinition("there are oranges", "StepDefs.there_are_oranges()")))
                .build()
                .run();

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"file:path/test1.feature\",\n" +
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
                "              \"location\": \"StepDefs.there_are_bananas()\"\n" +
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
                "  },\n" +
                "  {\n" +
                "    \"id\": \"orange-party\",\n" +
                "    \"uri\": \"file:path/test2.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Orange party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"orange-party;monkey-eats-oranges\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"start_timestamp\": \"1970-01-01T00:00:00.001Z\",\n" +
                "        \"name\": \"Monkey eats oranges\",\n" +
                "        \"line\": 3,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"name\": \"there are oranges\",\n" +
                "            \"line\": 4,\n" +
                "            \"match\": {\n" +
                "              \"location\": \"StepDefs.there_are_oranges()\"\n" +
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
