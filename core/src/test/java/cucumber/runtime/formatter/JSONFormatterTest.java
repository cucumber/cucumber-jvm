package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runtime.TestHelper;
import cucumber.runtime.model.CucumberFeature;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;
import org.mockito.stubbing.Answer;

import static cucumber.runtime.TestHelper.createEmbedHookAction;
import static cucumber.runtime.TestHelper.createWriteHookAction;
import static cucumber.runtime.TestHelper.result;
import static java.util.Arrays.asList;

public class JSONFormatterTest {

    @Test
    public void shouldHandleTestsBeingRunConcurrently() throws Exception {

        final List<String> features = asList("cucumber/runtime/formatter/FormatterParallelTests.feature", "cucumber/runtime/formatter/FormatterParallelTests2.feature");
        String actual = runFeaturesWithJSONPrettyFormatter(features,features.size());
        String expected = new Scanner(getClass().getResourceAsStream("JSONPrettyFormatterParallelExpected1.json"), "UTF-8").useDelimiter("\\A").next();
        String expected2 = new Scanner(getClass().getResourceAsStream("JSONPrettyFormatterParallelExpected2.json"), "UTF-8").useDelimiter("\\A").next();

        TestHelper.assertPrettyJsonEqualsOr(actual, expected, expected2);
    }

    @Test
    public void featureWithOutlineTest() throws Exception {
        String actual = runFeaturesWithJSONPrettyFormatter(asList("cucumber/runtime/formatter/JSONPrettyFormatterTest.feature"));
        String expected = new Scanner(getClass().getResourceAsStream("JSONPrettyFormatterTest.json"), "UTF-8").useDelimiter("\\A").next();

        TestHelper.assertPrettyJsonEquals(expected, actual);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_feature_and_scenario_with_tags() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "@Party @Banana\n" +
            "Feature: Banana party\n" +
            "  @Monkey\n" +
            "  Scenario: Monkey eats more bananas\n" +
            "    Then the monkey eats more bananas\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("the monkey eats more bananas", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("the monkey eats more bananas", "StepDefs.monkey_eats_more_bananas()");
        Long stepDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, stepDuration);

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
            "    \"uri\": \"path/test.feature\",\n" +
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
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_add_step_hooks_to_step() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: Banana party\n" +
            "\n" +
            "  Scenario: Monkey eats bananas\n" +
            "    Given there are bananas\n" +
            "    When monkey arrives\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("there are bananas", result("passed"));
        stepsToResult.put("monkey arrives", result("passed"));
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("there are bananas", "StepDefs.there_are_bananas()");
        stepsToLocation.put("monkey arrives", "StepDefs.monkey_arrives()");
        List<SimpleEntry<String, Result>> hooks = new ArrayList<SimpleEntry<String, Result>>();
        hooks.add(TestHelper.hookEntry("beforestep", result("passed")));
        hooks.add(TestHelper.hookEntry("afterstep", result("passed")));
        hooks.add(TestHelper.hookEntry("afterstep", result("passed")));
        List<String> hookLocations = new ArrayList<String>();
        hookLocations.add("Hooks.beforestep_hooks_1()");
        hookLocations.add("Hooks.afterstep_hooks_1()");
        hookLocations.add("Hooks.afterstep_hooks_2()");
        Long stepHookDuration = milliSeconds(1);

        String formatterOutput = runFeatureWithJSONPrettyFormatter(feature, stepsToResult, stepsToLocation, hooks, hookLocations, stepHookDuration);

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
            "                  \"location\": \"Hooks.afterstep_hooks_1()\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"result\": {\n" +
            "                  \"duration\": 1000000,\n" +
            "                  \"status\": \"passed\"\n" +
            "                },\n" +
            "                \"match\": {\n" +
            "                  \"location\": \"Hooks.afterstep_hooks_2()\"\n" +
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
            "                  \"location\": \"Hooks.afterstep_hooks_1()\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"result\": {\n" +
            "                  \"duration\": 1000000,\n" +
            "                  \"status\": \"passed\"\n" +
            "                },\n" +
            "                \"match\": {\n" +
            "                  \"location\": \"Hooks.afterstep_hooks_2()\"\n" +
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
            "    \"uri\": \"path/test.feature\",\n" +
            "    \"tags\": []\n" +
            "  }\n" +
            "]";
        assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_handle_write_from_a_hook() throws Throwable {
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_handle_embed_from_a_hook() throws Throwable {
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
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
    }

    @Test
    public void should_format_scenario_with_a_step_with_a_doc_string_and_content_type() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: Banana party\n" +
            "\n" +
            "  Scenario: Monkey eats bananas\n" +
            "    Given there are bananas\n" +
            "    \"\"\"doc\n" +
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
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
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
                "    ],\n" +
                "    \"tags\": []\n" +
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
                "    ],\n" +
                "    \"tags\": []\n" +
                "  }\n" +
                "]";
        TestHelper.assertPrettyJsonEquals(expected, formatterOutput);
    }

    private String runFeaturesWithJSONPrettyFormatter(final List<String> featurePaths) throws IOException {
        return runFeaturesWithJSONPrettyFormatter(featurePaths, 1L);
    }

    private String runFeaturesWithJSONPrettyFormatter(final List<String> featurePaths, final long threads) throws IOException {
        final File report = File.createTempFile("cucumber-jvm-json", ".json");
        return TestHelper.runFormatterWithPlugin("json", report.getAbsolutePath(), featurePaths, threads);
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

    private JSONFormatter createJsonFormatter(final StringBuilder report) {
        return new JSONFormatter(report);
    }

    private Long milliSeconds(int milliSeconds) {
        return milliSeconds * 1000000L;
    }
}
