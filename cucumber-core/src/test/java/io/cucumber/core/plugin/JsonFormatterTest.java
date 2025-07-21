package io.cucumber.core.plugin;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
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
import io.cucumber.docstring.DocString;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.UUID;

import static io.cucumber.core.backend.HookDefinition.HookType.AFTER_STEP;
import static io.cucumber.core.backend.HookDefinition.HookType.BEFORE;
import static io.cucumber.core.backend.HookDefinition.HookType.BEFORE_STEP;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Clock.fixed;
import static java.time.Duration.ofMillis;
import static java.time.Instant.EPOCH;
import static java.time.ZoneId.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonFormatterTest {

    final SourceReference monkeyArrives = getMethod("monkey_arrives");
    final SourceReference thereAreBananas = getMethod("there_are_bananas");
    final SourceReference thereAreOranges = getMethod("there_are_oranges");
    final SourceReference beforeHook1 = getMethod("before_hook_1");
    final SourceReference afterHook1 = getMethod("after_hook_1");
    final SourceReference beforeStepHook1 = getMethod("beforestep_hook_1");
    final SourceReference afterStepHook1 = getMethod("afterstep_hook_1");
    final SourceReference afterStepHook2 = getMethod("afterstep_hook_2");

    final SourceReference monkeyEatsBananas = getMethod("monkey_eats_bananas");
    final SourceReference monkeyEatsMoreBananas = getMethod("monkey_eats_more_bananas");

    private static SourceReference getMethod(String name) {
        try {
            return SourceReference.fromMethod(JsonFormatterTestStepDefinitions.class.getMethod(name));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void featureWithOutlineTest() throws JSONException {
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
                    singletonList(new StubHookDefinition(beforeHook1, BEFORE)),
                    asList(
                        new StubStepDefinition("bg_1", getMethod("bg_1")),
                        new StubStepDefinition("bg_2", getMethod("bg_2")),
                        new StubStepDefinition("bg_3", getMethod("bg_3")),
                        new StubStepDefinition("step_1", getMethod("step_1")),
                        new StubStepDefinition("step_2", getMethod("step_2")),
                        new StubStepDefinition("step_3", getMethod("step_3")),
                        new StubStepDefinition("cliché", getMethod("cliche")),
                        new StubStepDefinition("so_1 {int}", getMethod("so_1"), Integer.class),
                        new StubStepDefinition("so_2 {int} cucumbers", getMethod("so_2"), Integer.class),
                        new StubStepDefinition("{int} so_3", getMethod("so_3"), Integer.class),
                        new StubStepDefinition("a", getMethod("a")),
                        new StubStepDefinition("b", getMethod("b")),
                        new StubStepDefinition("c", getMethod("c"))),
                    emptyList()))
                .withAdditionalPlugins(new JsonFormatter(out));
    }

    @Test
    void featureWithOutlineTestParallel() throws JSONException {
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
    void should_format_scenario_with_an_undefined_step() throws JSONException {
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

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) throws JSONException {
        assertJsonEquals(expected, new String(actual.toByteArray(), UTF_8));

    }

    private void assertJsonEquals(String expected, String actual) throws JSONException {
        assertEquals(expected, actual, true);
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

    @Test
    void should_format_scenario_with_a_failed_step() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas,
                        new StubException("the stack trace"))))
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
    void should_format_scenario_with_a_rule() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas)))
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
                "        \"id\": \"banana-party;this-is-all-monkey-business;monkey-eats-bananas\",\n" +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
    void should_format_scenario_with_a_rule_and_background() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas)))
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
                "        \"id\": \"banana-party;this-is-all-monkey-business;monkey-eats-bananas\",\n" +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
    void should_format_scenario_outline_with_one_example() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas)))
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

    @Test
    void should_format_feature_with_background() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas),
                    new StubStepDefinition("the monkey eats bananas", monkeyEatsBananas),
                    new StubStepDefinition("the monkey eats more bananas", monkeyEatsMoreBananas)))
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.monkey_eats_bananas()\"\n"
                +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.monkey_eats_more_bananas()\"\n"
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

    @Test
    void should_format_feature_and_scenario_with_tags() throws JSONException {
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
                    new StubStepDefinition("the monkey eats more bananas", monkeyEatsMoreBananas)))
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.monkey_eats_more_bananas()\"\n"
                +
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
    void should_format_scenario_with_hooks() throws JSONException {
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
                    singletonList(new StubHookDefinition(beforeHook1, HookDefinition.HookType.BEFORE)),
                    singletonList(new StubStepDefinition("there are bananas", thereAreBananas)),
                    singletonList(new StubHookDefinition(afterHook1, HookDefinition.HookType.AFTER))))
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.before_hook_1()\"\n"
                +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.after_hook_1()\"\n"
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

    @Test
    void should_add_step_hooks_to_step() throws JSONException {
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
                    singletonList(new StubHookDefinition(beforeStepHook1, BEFORE_STEP)),
                    asList(
                        new StubStepDefinition("there are bananas", thereAreBananas),
                        new StubStepDefinition("monkey arrives", monkeyArrives)),
                    asList(
                        new StubHookDefinition(afterStepHook1, AFTER_STEP),
                        new StubHookDefinition(afterStepHook2, AFTER_STEP)),
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
                "                  \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.beforestep_hook_1()\"\n"
                +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"line\": 4,\n" +
                "            \"name\": \"there are bananas\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()\"\n"
                +
                "            },\n" +
                "            \"after\": [\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.afterstep_hook_2()\"\n"
                +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.afterstep_hook_1()\"\n"
                +
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
                "                  \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.beforestep_hook_1()\"\n"
                +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"line\": 5,\n" +
                "            \"name\": \"monkey arrives\",\n" +
                "            \"match\": {\n" +
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.monkey_arrives()\"\n"
                +
                "            },\n" +
                "            \"after\": [\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.afterstep_hook_2()\"\n"
                +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"result\": {\n" +
                "                  \"duration\": 1000000,\n" +
                "                  \"status\": \"passed\"\n" +
                "                },\n" +
                "                \"match\": {\n" +
                "                  \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.afterstep_hook_1()\"\n"
                +
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
    void should_handle_write_from_a_hook() throws JSONException {
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
                    singletonList(new StubHookDefinition(beforeHook1, BEFORE,
                        testCaseState -> testCaseState.log("printed from hook"))),
                    singletonList(new StubStepDefinition("there are bananas", thereAreBananas)),
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.before_hook_1()\"\n"
                +
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

    @Test
    void should_handle_embed_from_a_hook() throws JSONException {
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
                    singletonList(new StubHookDefinition(beforeHook1,
                        BEFORE,
                        testCaseState -> testCaseState
                                .attach(new byte[] { 1, 2, 3 }, "mime-type;base64", null))),
                    singletonList(new StubStepDefinition("there are bananas", thereAreBananas)),
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.before_hook_1()\"\n"
                +
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

    @Test
    void should_handle_embed_with_name_from_a_hook() throws JSONException {
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
                    singletonList(new StubHookDefinition(beforeHook1, BEFORE,
                        testCaseState -> testCaseState.attach(new byte[] { 1, 2, 3 }, "mime-type;base64",
                            "someEmbedding"))),
                    singletonList(new StubStepDefinition("there are bananas", thereAreBananas)),
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.before_hook_1()\"\n"
                +
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

    @Test
    void should_format_scenario_with_a_step_with_a_doc_string() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas, String.class)))
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

    @Test
    void should_format_scenario_with_a_step_with_a_doc_string_and_content_type() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "    \"\"\"text/plain\n" +
                "    doc string content\n" +
                "    \"\"\"\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", thereAreBananas, DocString.class)))
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
                "              \"content_type\": \"text/plain\",\n" +
                "              \"value\": \"doc string content\",\n" +
                "              \"line\": 5\n" +
                "            },\n" +
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

    @Test
    void should_format_scenario_with_a_step_with_a_data_table() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas, DataTable.class)))
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

    @Test
    void should_handle_several_features() throws JSONException {
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
                    new StubStepDefinition("there are bananas", thereAreBananas),
                    new StubStepDefinition("there are oranges", thereAreOranges)))
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
                "              \"location\": \"io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_oranges()\"\n"
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
