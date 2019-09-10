package io.cucumber.jupiter.engine;

import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;
import org.junit.jupiter.api.Test;

import static io.cucumber.jupiter.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.jupiter.engine.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.jupiter.engine.Constants.EXECUTION_STRICT_PROPERTY_NAME;
import static io.cucumber.jupiter.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.jupiter.engine.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CucumberEngineOptionsTest {

    @Test
    void getPluginNames() {
        assertEquals(
            singletonList("html:path/to/report.html"),
            new CucumberEngineOptions(
                new MapConfigurationParameters(PLUGIN_PROPERTY_NAME, "html:path/to/report.html")
            ).plugins().stream().map(Options.Plugin::pluginString).collect(toList())
        );
        assertEquals(
            asList("html:path/with spaces/to/report.html", "json:path/with spaces/to/report.json"),
            new CucumberEngineOptions(
                new MapConfigurationParameters(PLUGIN_PROPERTY_NAME, "html:path/with spaces/to/report.html, json:path/with spaces/to/report.json")
            ).plugins().stream().map(Options.Plugin::pluginString).collect(toList())
        );
    }

    @Test
    void isStrict() {
        assertTrue(new CucumberEngineOptions(new MapConfigurationParameters(EXECUTION_STRICT_PROPERTY_NAME, "true")).isStrict());
        assertFalse(new CucumberEngineOptions(new MapConfigurationParameters(EXECUTION_STRICT_PROPERTY_NAME, "false")).isStrict());
    }

    @Test
    void isMonochrome() {
        assertTrue(new CucumberEngineOptions(new MapConfigurationParameters(ANSI_COLORS_DISABLED_PROPERTY_NAME, "true")).isMonochrome());
        assertFalse(new CucumberEngineOptions(new MapConfigurationParameters(ANSI_COLORS_DISABLED_PROPERTY_NAME, "false")).isMonochrome());
    }

    @Test
    void getGlue() {
        //TODO: Implement test
    }

    @Test
    void isDryRun() {
        assertTrue(new CucumberEngineOptions(new MapConfigurationParameters(EXECUTION_DRY_RUN_PROPERTY_NAME, "true")).isDryRun());
        assertFalse(new CucumberEngineOptions(new MapConfigurationParameters(EXECUTION_DRY_RUN_PROPERTY_NAME, "false")).isDryRun());
    }

    @Test
    void getSnippetType() {
        assertEquals(SnippetType.UNDERSCORE, new CucumberEngineOptions(new MapConfigurationParameters(SNIPPET_TYPE_PROPERTY_NAME, "underscore")).getSnippetType());
        assertEquals(SnippetType.CAMELCASE, new CucumberEngineOptions(new MapConfigurationParameters(SNIPPET_TYPE_PROPERTY_NAME, "camelcase")).getSnippetType());
    }
}