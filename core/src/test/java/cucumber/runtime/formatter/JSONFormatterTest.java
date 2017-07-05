package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runner.TimeService;
import cucumber.runtime.Backend;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.TestHelper;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.deps.com.google.gson.JsonParser;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import gherkin.deps.com.google.gson.JsonElement;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.AbstractMap.SimpleEntry;

import static cucumber.runtime.TestHelper.result;
import static cucumber.runtime.TestHelper.createEmbedHookAction;
import static cucumber.runtime.TestHelper.createWriteHookAction;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONFormatterTest {

    @Test
    public void featureWithOutlineTest() throws Exception {
        String actual = runFeaturesWithJSONPrettyFormatter(asList("cucumber/runtime/formatter/JSONPrettyFormatterTest.feature"));
        String expected = new Scanner(getClass().getResourceAsStream("JSONPrettyFormatterTest.json"), "UTF-8").useDelimiter("\\A").next();

        assertPrettyJsonEquals(expected, actual);
    }

    @Test
    public void should_format_scenario_with_an_undefined_step() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("undefined"));

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_with_a_passed_step() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_with_a_failed_step() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("failed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_outline_with_one_example() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Fruit party\n" +
                "\n" +
                "  Scenario Outline: Monkey eats fruits\n" +
                "    Given there are <fruits>\n" +
                "      Examples: Fruit table\n" +
                "      | fruits  |\n" +
                "      | bananas |\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"fruit-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Fruit party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"fruit-party;monkey-eats-fruits;fruit-table;2\",\n" +
                "        \"keyword\": \"Scenario Outline\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_feature_with_background() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
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
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        stepsToResult.put("the monkey eats bananas", result("passed"));
        stepsToResult.put("the monkey eats more bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        stepsToLocation.put("the monkey eats bananas", "StepDefs.monkey_eats_bananas()");
        stepsToLocation.put("the monkey eats more bananas", "StepDefs.monkey_eats_more_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_with_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        List<String> hookLocations = new ArrayList<String>();
        hookLocations.add("Hooks.before_hook_1()");
        hookLocations.add("Hooks.after_hook_1()");
        Long stepHookDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, hooks, hookLocations, stepHookDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_handle_write_from_a_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        List<String> hookLocations = new ArrayList<String>();
        hookLocations.add("Hooks.before_hook_1()");
        List<Answer<Object>> hookActions = new ArrayList<Answer<Object>>();
        hookActions.add(createWriteHookAction("printed from hook"));
        Long stepHookDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, hooks, hookLocations, hookActions, stepHookDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_handle_embed_from_a_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        List<String> hookLocations = new ArrayList<String>();
        hookLocations.add("Hooks.before_hook_1()");
        List<Answer<Object>> hookActions = new ArrayList<Answer<Object>>();
        hookActions.add(createEmbedHookAction(new byte[]{1, 2, 3}, "mime-type;base64"));
        Long stepHookDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, hooks, hookLocations, hookActions, stepHookDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"name\": \"Monkey eats bananas\",\n" +
                "        \"line\": 3,\n" +
                "        \"description\": \"\",\n" +
                "        \"type\": \"scenario\",\n" +
                "        \"before\": [\n" +
                "          {\n" +
                "            \"match\": {\n" +
                "              \"location\": \"Hooks.before_hook_1()\"\n" +
                "            },\n" +
                "            \"embedding\": [\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_with_a_step_with_a_doc_string() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "    \"\"\"\n" +
                "    doc string content\n" +
                "    \"\"\"\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_with_a_step_with_a_data_table() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n" +
                "      | aa | 11 |\n" +
                "      | bb | 22 |\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_handle_several_features() throws Throwable {
        CucumberFeature feature1 = TestHelper.feature("path/test1.feature", "" +
                "Feature: Banana party\n" +
                "\n" +
                "  Scenario: Monkey eats bananas\n" +
                "    Given there are bananas\n");
        CucumberFeature feature2 = TestHelper.feature("path/test2.feature", "" +
                "Feature: Orange party\n" +
                "\n" +
                "  Scenario: Monkey eats oranges\n" +
                "    Given there are oranges\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        stepsToResult.put("there are oranges", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        stepsToLocation.put("there are oranges", "StepDefs.there_are_oranges()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeaturesWithJSONPrettyFormatter(asList(feature1, feature2), stepsToResult, stepsToLocation, stepDuration);

        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"banana-party\",\n" +
                "    \"uri\": \"path/test1.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Banana party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"banana-party;monkey-eats-bananas\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"orange-party\",\n" +
                "    \"uri\": \"path/test2.feature\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"name\": \"Orange party\",\n" +
                "    \"line\": 1,\n" +
                "    \"description\": \"\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"id\": \"orange-party;monkey-eats-oranges\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
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
                "    ]\n" +
                "  }\n" +
                "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    private void assertPrettyJsonEquals(final String expected, final String actual) {
        assertJsonEquals(expected, actual);

        List<String> expectedLines = sortedLinesWithWhitespace(expected);
        List<String> actualLines = sortedLinesWithWhitespace(actual);
        assertEquals(expectedLines, actualLines);
    }

    private List<String> sortedLinesWithWhitespace(final String string) {
        List<String> lines = asList(string.split(",?(?:\r\n?|\n)")); // also remove trailing ','
        sort(lines);
        return lines;
    }

    private void assertJsonEquals(final String expected, final String actual) {
        JsonParser parser = new JsonParser();
        JsonElement o1 = parser.parse(expected);
        JsonElement o2 = parser.parse(actual);
        assertEquals(o1, o2);
    }

    private String runFeaturesWithJSONPrettyFormatter(final List<String> featurePaths) throws IOException {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(PickleTag.class))).thenReturn(true);
        File report = File.createTempFile("cucumber-jvm-junit", ".json");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

        List<String> args = new ArrayList<String>();
        args.add("--plugin");
        args.add("json:" + report.getAbsolutePath());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(args);
        Backend backend = mock(Backend.class);
        when(backend.getSnippet(any(PickleStep.class), anyString(), any(FunctionNameGenerator.class))).thenReturn("TEST SNIPPET");
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, new TimeService.Stub(1234), null);
        runtime.getGlue().addBeforeHook(hook);
        runtime.run();
        Scanner scanner = new Scanner(new FileInputStream(report), "UTF-8");
        String formatterOutput = scanner.useDelimiter("\\A").next();
        scanner.close();
        return formatterOutput;
    }

    private String runFeatureWithJSONPrettyFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult)
            throws Throwable {
        return runFeatureWithJSONPrettyFormatter(feature, stepsToResult, Collections.<String, String>emptyMap(), milliSeconds(0));
    }

    private String runFeatureWithJSONPrettyFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final long stepHookDuration) throws Throwable {
        return runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, Collections.<SimpleEntry<String, Result>>emptyList(), stepHookDuration);
    }

    private String runFeatureWithJSONPrettyFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final long stepHookDuration) throws Throwable {
        return runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, hooks, Collections.<String>emptyList(), stepHookDuration);
    }

    private String runFeatureWithJSONPrettyFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations, final long stepHookDuration) throws Throwable {
        return runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, hooks, hookLocations, Collections.<Answer<Object>>emptyList(), stepHookDuration);
    }

    private String runFeatureWithJSONPrettyFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations, final List<Answer<Object>> hookActions, final long stepHookDuration) throws Throwable {
        return runFeaturesWithJSONPrettyFormatter(asList(feature), stepsToResult, stepsToLocation, hooks, hookLocations, hookActions, stepHookDuration);
    }

    private String runFeaturesWithJSONPrettyFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final Long stepHookDuration) throws Throwable {
        return runFeaturesWithJSONPrettyFormatter(features, stepsToResult, stepsToLocation, Collections.<SimpleEntry<String, Result>>emptyList(), Collections.<String>emptyList(), Collections.<Answer<Object>>emptyList(), stepHookDuration);
    }

    private String runFeaturesWithJSONPrettyFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations, final List<Answer<Object>> hookActions, final Long stepHookDuration) throws Throwable {
        final StringBuilder report = new StringBuilder();
        final JSONFormatter jsonFormatter = createJsonFormatter(report);
        TestHelper.runFeaturesWithFormatter(features, stepsToResult, stepsToLocation, hooks, hookLocations, hookActions, stepHookDuration, jsonFormatter);
        return report.toString();
    }

    private JSONFormatter createJsonFormatter(final StringBuilder report) throws IOException {
        return new JSONFormatter(report);
    }

    private Long milliSeconds(int milliSeconds) {
        return milliSeconds * 1000000L;
    }
}
