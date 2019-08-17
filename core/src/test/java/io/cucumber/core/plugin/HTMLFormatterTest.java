package io.cucumber.core.plugin;

import gherkin.deps.com.google.gson.JsonParser;
import io.cucumber.core.event.Result;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.runner.TestHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.runner.TestHelper.createEmbedHookAction;
import static io.cucumber.core.runner.TestHelper.createWriteHookAction;
import static io.cucumber.core.runner.TestHelper.result;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

class HTMLFormatterTest {

    private final static String jsFunctionCallRegexString = "formatter.(\\w*)\\(([^)]*)\\);";
    private final static Pattern jsFunctionCallRegex = Pattern.compile(jsFunctionCallRegexString);

    private final List<CucumberFeature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();
    private final List<Answer<Object>> hookActions = new ArrayList<>();
    private Duration stepDuration = null;

    private URL outputDir;

    private void writeReport() throws Throwable {
        outputDir = TempDir.createTempDirectory().toURI().toURL();
        runFeaturesWithFormatter(outputDir);
    }

    @Test
    void writes_index_html() throws Throwable {
        writeReport();
        URL indexHtml = new URL(outputDir, "index.html");
        Document document = Jsoup.parse(new File(indexHtml.getFile()), UTF_8.name());
        Element reportElement = document.body().getElementsByClass("cucumber-report").first();
        assertThat(reportElement.text(), is(equalTo("")));
    }

    @Test
    void writes_valid_report_js() throws Throwable {
        writeReport();
        assertJsFunctionCallSequence(asList("" +
                "formatter.uri(\"file:some/path/some.feature\");\n",
            "formatter.feature({\n" +
                "  \"name\": \"\",\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Feature\"\n" +
                "});\n",
            "formatter.scenario({\n" +
                "  \"name\": \"some cukes\",\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\"\n" +
                "});\n",
            "formatter.step({\n" +
                "  \"name\": \"first step\",\n" +
                "  \"keyword\": \"Given \"\n" +
                "});\n",
            "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n",
            "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n",
            "formatter.embedding(\"text/plain\", \"dodgy stack trace here\", null);\n",
            "formatter.after({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n",
            "formatter.embedding(\"image/png\", \"embedded0.png\", \"Fake image\");\n",
            "formatter.after({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"

            ),
            readReportJs());
    }

    @Test
    void includes_uri() throws Throwable {
        writeReport();
        assertContains("formatter.uri(\"file:some/path/some.feature\");", readReportJs());
    }

    @Test
    void included_embedding() throws Throwable {
        writeReport();
        String reportJs = readReportJs();
        assertAll("Checking ReportJs",
            () -> assertContains("formatter.embedding(\"image/png\", \"embedded0.png\", \"Fake image\");", reportJs),
            () -> assertContains("formatter.embedding(\"text/plain\", \"dodgy stack trace here\", null);", reportJs)
        );
    }

    @Test
    void should_handle_a_single_scenario() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n" +
            "    Then second step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.uri(\"file:path/test.feature\");\n", "" +
                "formatter.feature({\n" +
                "  \"description\": \"\",\n" +
                "  \"name\": \"feature name\",\n" +
                "  \"keyword\": \"Feature\"\n" +
                "});\n", "" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Then \",\n" +
                "  \"name\": \"second step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:7\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});"),
            formatterOutput);
    }

    @Test
    void should_handle_backgound() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background name\n" +
            "    Given first step\n" +
            "  Scenario: scenario 1\n" +
            "    Then second step\n" +
            "  Scenario: scenario 2\n" +
            "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.background({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Background\",\n" +
                "  \"name\": \"background name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario 1\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Then \",\n" +
                "  \"name\": \"second step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:7\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.background({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Background\",\n" +
                "  \"name\": \"background name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario 2\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Then \",\n" +
                "  \"name\": \"third step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:11\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_scenario_outline() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario Outline: outline name\n" +
            "    Given first step\n" +
            "    Then <arg> step\n" +
            "    Examples: examples name\n" +
            "      |  arg   |\n" +
            "      | second |\n" +
            "      | third  |\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.uri(\"file:path/test.feature\");\n", "" +
                "formatter.feature({\n" +
                "  \"description\": \"\",\n" +
                "  \"name\": \"feature name\",\n" +
                "  \"keyword\": \"Feature\"\n" +
                "});\n", "" +
                "formatter.scenarioOutline({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario Outline\",\n" +
                "  \"name\": \"outline name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Then \",\n" +
                "  \"name\": \"\\u003carg\\u003e step\"\n" +
                "});\n", "" +
                "formatter.examples({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Examples\",\n" +
                "  \"name\": \"examples name\",\n" +
                "  \"rows\": [\n" +
                "    {\n" +
                "      \"cells\": [\n" +
                "        \"arg\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"cells\": [\n" +
                "        \"second\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"cells\": [\n" +
                "        \"third\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "});\n", "" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario Outline\",\n" +
                "  \"name\": \"outline name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Then \",\n" +
                "  \"name\": \"second step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:7\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario Outline\",\n" +
                "  \"name\": \"outline name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Then \",\n" +
                "  \"name\": \"third step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:11\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});"),
            formatterOutput);
    }

    @Test
    void should_handle_before_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.before({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_after_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.after({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_after_step_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n" +
            "    When second step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:4");
        hooks.add(TestHelper.hookEntry("afterstep", result("passed")));
        hooks.add(TestHelper.hookEntry("afterstep", result("passed")));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.afterstep({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.afterstep({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"When \",\n" +
                "  \"name\": \"second step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:4\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.afterstep({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.afterstep({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_output_from_before_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hookActions.add(createWriteHookAction("printed from hook"));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.write(\"printed from hook\");\n", "" +
                "formatter.before({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_output_from_after_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        hookActions.add(createWriteHookAction("printed from hook"));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.write(\"printed from hook\");\n", "" +
                "formatter.after({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_output_from_after_step_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n" +
            "    When second step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:4");
        hooks.add(TestHelper.hookEntry("afterstep", result("passed")));
        hookActions.add(createWriteHookAction("printed from hook"));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.write(\"printed from hook\");\n", "" +
                "formatter.afterstep({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"When \",\n" +
                "  \"name\": \"second step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:4\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.write(\"printed from hook\");\n", "" +
                "formatter.afterstep({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    @Test
    void should_handle_text_embeddings_from_before_hooks() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hookActions.add(createEmbedHookAction("embedded from hook".getBytes(US_ASCII), "text/ascii"));
        stepDuration = ofMillis(1L);

        String formatterOutput = runFeaturesWithFormatter();

        assertJsFunctionCallSequence(asList("" +
                "formatter.scenario({\n" +
                "  \"description\": \"\",\n" +
                "  \"keyword\": \"Scenario\",\n" +
                "  \"name\": \"scenario name\"\n" +
                "});\n", "" +
                "formatter.embedding(\"text/ascii\", \"embedded from hook\", null);\n", "" +
                "formatter.before({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n", "" +
                "formatter.step({\n" +
                "  \"keyword\": \"Given \",\n" +
                "  \"name\": \"first step\"\n" +
                "});\n", "" +
                "formatter.match({\n" +
                "  \"location\": \"path/step_definitions.java:3\"\n" +
                "});\n", "" +
                "formatter.result({\n" +
                "  \"status\": \"passed\"\n" +
                "});\n"),
            formatterOutput);
    }

    private String readReportJs() throws IOException {
        InputStream reportJsStream = new URL(outputDir, "report.js").openStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(reportJsStream, UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private void assertJsFunctionCallSequence(List<String> expectedList, String actual) {
        Iterator<String> expectedIterator = expectedList.iterator();
        String expected = expectedIterator.next();
        Matcher expectedMatcher = jsFunctionCallRegex.matcher(expected);
        Matcher actualMatcher = jsFunctionCallRegex.matcher(actual);
        assertThat(jsFunctionCallMatchFailure(expected), expectedMatcher.find(), is(equalTo(true)));
        boolean found = false;
        while (actualMatcher.find()) {
            if (matchFound(expectedMatcher, actualMatcher)) {
                found = true;
                break;
            }
        }
        assertThat(jsFunctionCallNotFoundMessage(actual, expected), found, is(equalTo(true)));
        while (expectedIterator.hasNext()) {
            expected = expectedIterator.next();
            expectedMatcher = jsFunctionCallRegex.matcher(expected);
            assertThat(jsFunctionCallMatchFailure(expected), expectedMatcher.find(), is(equalTo(true)));
            assertThat(jsFunctionCallNotFoundMessage(actual, expected), actualMatcher.find(), is(equalTo(true)));
            if (!matchFound(expectedMatcher, actualMatcher)) {
                fail(jsFunctionCallNotFoundMessage(actual, expected));
            }
        }
    }

    private String jsFunctionCallMatchFailure(String expected) {
        return "The expected string: " + expected + ", does not match " + jsFunctionCallRegexString;
    }

    private String jsFunctionCallNotFoundMessage(String actual, String expected) {
        return "The expected js function call: " + expected + ", is not found in " + actual;
    }

    private boolean matchFound(Matcher expectedMatcher, Matcher actualMatcher) {
        String expectedFunction = expectedMatcher.group(1);
        String actualFunction = actualMatcher.group(1);
        if (!expectedFunction.equals(actualFunction)) {
            return false;
        }
        String expectedArgument = expectedMatcher.group(2);
        String actualArgumant = actualMatcher.group(2);
        if (matchUsingJson(expectedArgument, actualArgumant)) {
            JsonParser parser = new JsonParser();
            return parser.parse(expectedArgument).equals(parser.parse(actualArgumant));
        } else {
            return expectedArgument.equals(actualArgumant);
        }
    }

    private boolean matchUsingJson(String expected, String actual) {
        return expected.startsWith("{") && actual.startsWith("{");
    }

    private void assertContains(String substring, String string) {
        if (!string.contains(substring)) {
            fail(String.format("[%s] not contained in [%s]", substring, string));
        }
    }

    private void runFeaturesWithFormatter(URL outputDir) {
        final HTMLFormatter f = new HTMLFormatter(outputDir);
        CucumberFeature feature = TestFeatureParser.parse("some/path/some.feature", "" +
            "Feature:\n" +
            "  Scenario: some cukes\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        hookActions.add(createEmbedHookAction("fakedata".getBytes(US_ASCII), "image/png", "Fake image"));
        hookActions.add(createEmbedHookAction("dodgy stack trace here".getBytes(US_ASCII), "text/plain"));
        stepDuration = ofMillis(1L);

        runFeaturesWithFormatter(f);
    }

    private String runFeaturesWithFormatter() {
        final StringBuilder report = new StringBuilder();
        final HTMLFormatter formatter = new HTMLFormatter(null, new NiceAppendable(report));
        runFeaturesWithFormatter(formatter);
        return report.toString();
    }

    private void runFeaturesWithFormatter(HTMLFormatter formatter) {
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
    }

}
