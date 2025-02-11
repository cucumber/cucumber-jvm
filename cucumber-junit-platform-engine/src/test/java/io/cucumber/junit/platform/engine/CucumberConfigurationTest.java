package io.cucumber.junit.platform.engine;

import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;

import java.net.URI;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CucumberConfigurationTest {

    @Test
    void getPluginNames() {
        ConfigurationParameters config = new MapConfigurationParameters(
            Constants.PLUGIN_PROPERTY_NAME,
            "html:path/to/report.html");

        assertThat(new CucumberConfiguration(config).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            hasItem("html:path/to/report.html"));

        CucumberConfiguration htmlAndJson = new CucumberConfiguration(
            new MapConfigurationParameters(Constants.PLUGIN_PROPERTY_NAME,
                "html:path/with spaces/to/report.html, message:path/with spaces/to/report.ndjson"));

        assertThat(htmlAndJson.plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            hasItems("html:path/with spaces/to/report.html", "message:path/with spaces/to/report.ndjson"));
    }

    @Test
    void getPluginNamesWithPublishToken() {
        ConfigurationParameters config = new MapConfigurationParameters(
            Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME, "some/token");

        assertThat(new CucumberConfiguration(config).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            hasItem("io.cucumber.core.plugin.PublishFormatter:some/token"));
    }

    @Test
    void getPluginNamesWithNothingEnabled() {
        ConfigurationParameters config = new EmptyConfigurationParameters();

        assertThat(new CucumberConfiguration(config).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            empty());
    }

    @Test
    void getPluginNamesWithPublishQuiteEnabled() {
        ConfigurationParameters config = new MapConfigurationParameters(
            Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true");

        assertThat(new CucumberConfiguration(config).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            empty());
    }

    @Test
    void getPluginNamesWithPublishEnabled() {
        ConfigurationParameters config = new MapConfigurationParameters(
            Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, "true");

        assertThat(new CucumberConfiguration(config).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            hasItem("io.cucumber.core.plugin.PublishFormatter"));
    }

    @Test
    void getPluginNamesWithPublishDisabledAndPublishToken() {
        ConfigurationParameters config = new MapConfigurationParameters(Map.of(
            Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, "false",
            Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME, "some/token"));

        assertThat(new CucumberConfiguration(config).plugins().stream()
                .map(Options.Plugin::pluginString)
                .collect(toList()),
            empty());
    }

    @Test
    void isMonochrome() {
        MapConfigurationParameters ansiColors = new MapConfigurationParameters(
            Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME,
            "true");
        assertTrue(new CucumberConfiguration(ansiColors).isMonochrome());

        MapConfigurationParameters noAnsiColors = new MapConfigurationParameters(
            Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME,
            "false");
        assertFalse(new CucumberConfiguration(noAnsiColors).isMonochrome());
    }

    @Test
    void getGlue() {
        ConfigurationParameters config = new MapConfigurationParameters(
            Constants.GLUE_PROPERTY_NAME,
            "com.example.app, com.example.glue");

        assertThat(new CucumberConfiguration(config).getGlue(),
            contains(
                URI.create("classpath:/com/example/app"),
                URI.create("classpath:/com/example/glue")));
    }

    @Test
    void isDryRun() {
        ConfigurationParameters dryRun = new MapConfigurationParameters(
            Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,
            "true");
        assertTrue(new CucumberConfiguration(dryRun).isDryRun());

        ConfigurationParameters noDryRun = new MapConfigurationParameters(
            Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,
            "false");
        assertFalse(new CucumberConfiguration(noDryRun).isDryRun());
    }

    @Test
    void getSnippetType() {
        ConfigurationParameters underscore = new MapConfigurationParameters(
            Constants.SNIPPET_TYPE_PROPERTY_NAME,
            "underscore");

        assertThat(new CucumberConfiguration(underscore).getSnippetType(), is(SnippetType.UNDERSCORE));

        ConfigurationParameters camelcase = new MapConfigurationParameters(
            Constants.SNIPPET_TYPE_PROPERTY_NAME,
            "camelcase");
        assertThat(new CucumberConfiguration(camelcase).getSnippetType(), is(SnippetType.CAMELCASE));
    }

    @Test
    void isParallelExecutionEnabled() {
        ConfigurationParameters enabled = new MapConfigurationParameters(
            Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME,
            "true");
        assertTrue(new CucumberConfiguration(enabled).isParallelExecutionEnabled());

        ConfigurationParameters disabled = new MapConfigurationParameters(
            Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME,
            "false");
        assertFalse(new CucumberConfiguration(disabled).isParallelExecutionEnabled());
        ConfigurationParameters absent = new MapConfigurationParameters(
            "some key", "some value");
        assertFalse(new CucumberConfiguration(absent).isParallelExecutionEnabled());
    }

    @Test
    void objectFactory() {
        ConfigurationParameters configurationParameters = new MapConfigurationParameters(
            Constants.OBJECT_FACTORY_PROPERTY_NAME,
            DefaultObjectFactory.class.getName());

        assertThat(new CucumberConfiguration(configurationParameters).getObjectFactoryClass(),
            is(DefaultObjectFactory.class));
    }

    @Test
    void uuidGenerator() {
        ConfigurationParameters configurationParameters = new MapConfigurationParameters(
            Constants.UUID_GENERATOR_PROPERTY_NAME,
            IncrementingUuidGenerator.class.getName());

        assertThat(new CucumberConfiguration(configurationParameters).getUuidGeneratorClass(),
            is(IncrementingUuidGenerator.class));
    }
}
