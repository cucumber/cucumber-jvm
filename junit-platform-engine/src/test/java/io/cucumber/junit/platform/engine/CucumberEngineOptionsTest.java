package io.cucumber.junit.platform.engine;

import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CucumberEngineOptionsTest {

    @Test
    void getPluginNames() {
        MapConfigurationParameters html = new MapConfigurationParameters(
            Constants.PLUGIN_PROPERTY_NAME,
            "html:path/to/report.html"
        );

        assertEquals(
            singletonList("html:path/to/report.html"),
            new CucumberEngineOptions(html).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList())
        );

        CucumberEngineOptions htmlAndJson = new CucumberEngineOptions(
            new MapConfigurationParameters(Constants.PLUGIN_PROPERTY_NAME, "html:path/with spaces/to/report.html, json:path/with spaces/to/report.json")
        );
        assertEquals(
            asList("html:path/with spaces/to/report.html", "json:path/with spaces/to/report.json"),
            htmlAndJson.plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList())
        );
    }

    @Test
    void isStrict() {
        MapConfigurationParameters someConfig = new MapConfigurationParameters(
            "some key", "some value"
        );
        assertTrue(new CucumberEngineOptions(someConfig).isStrict());
    }

    @Test
    void isMonochrome() {
        MapConfigurationParameters ansiColors = new MapConfigurationParameters(
            Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME,
            "true"
        );
        assertTrue(new CucumberEngineOptions(ansiColors).isMonochrome());

        MapConfigurationParameters noAnsiColors = new MapConfigurationParameters(
            Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME,
            "false"
        );
        assertFalse(new CucumberEngineOptions(noAnsiColors).isMonochrome());
    }

    @Test
    void getGlue() {
        MapConfigurationParameters glue = new MapConfigurationParameters(
            Constants.GLUE_PROPERTY_NAME,
            "com.example.app, com.example.glue"
        );
        assertEquals(
            asList(URI.create("classpath:/com/example/app"), URI.create("classpath:/com/example/glue")),
            new CucumberEngineOptions(glue).getGlue()
        );
    }

    @Test
    void isDryRun() {
        MapConfigurationParameters dryRun = new MapConfigurationParameters(
            Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,
            "true"
        );
        assertTrue(new CucumberEngineOptions(dryRun).isDryRun());

        MapConfigurationParameters noDryRun = new MapConfigurationParameters(
            Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,
            "false"
        );
        assertFalse(new CucumberEngineOptions(noDryRun).isDryRun());
    }

    @Test
    void getSnippetType() {
        MapConfigurationParameters underscore = new MapConfigurationParameters(
            Constants.SNIPPET_TYPE_PROPERTY_NAME,
            "underscore"
        );
        assertEquals(SnippetType.UNDERSCORE, new CucumberEngineOptions(underscore).getSnippetType());

        MapConfigurationParameters camelcase = new MapConfigurationParameters(
            Constants.SNIPPET_TYPE_PROPERTY_NAME,
            "camelcase"
        );
        assertEquals(SnippetType.CAMELCASE, new CucumberEngineOptions(camelcase).getSnippetType());
    }
}