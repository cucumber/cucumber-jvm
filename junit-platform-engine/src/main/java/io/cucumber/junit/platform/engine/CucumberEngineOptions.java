package io.cucumber.junit.platform.engine;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.ObjectFactoryParser;
import io.cucumber.core.options.PluginOption;
import io.cucumber.core.options.SnippetTypeParser;
import io.cucumber.core.snippets.SnippetType;
import org.junit.platform.engine.ConfigurationParameters;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.SNIPPET_TYPE_PROPERTY_NAME;

class CucumberEngineOptions implements
    io.cucumber.core.plugin.Options,
    io.cucumber.core.runner.Options,
    io.cucumber.core.backend.Options {

    private final ConfigurationParameters configurationParameters;

    CucumberEngineOptions(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @Override
    public boolean isStrict() {
        return true;
    }

    @Override
    public List<Plugin> plugins() {
        return configurationParameters.get(PLUGIN_PROPERTY_NAME, s -> Arrays.stream(s.split(","))
            .map(String::trim)
            .map(PluginOption::parse)
            .map(pluginOption -> (Plugin) pluginOption)
            .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    @Override
    public boolean isMonochrome() {
        return configurationParameters
            .getBoolean(ANSI_COLORS_DISABLED_PROPERTY_NAME)
            .orElse(false);
    }

    @Override
    public List<URI> getGlue() {
        return configurationParameters
            .get(GLUE_PROPERTY_NAME, s -> Arrays.asList(s.split(",")))
            .orElse(Collections.singletonList(CLASSPATH_SCHEME_PREFIX))
            .stream()
            .map(String::trim)
            .map(GluePath::parse)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isDryRun() {
        return configurationParameters
            .getBoolean(EXECUTION_DRY_RUN_PROPERTY_NAME)
            .orElse(false);
    }

    @Override
    public SnippetType getSnippetType() {
        return configurationParameters
            .get(SNIPPET_TYPE_PROPERTY_NAME, SnippetTypeParser::parseSnippetType)
            .orElse(SnippetType.UNDERSCORE);
    }

    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return configurationParameters
            .get(OBJECT_FACTORY_PROPERTY_NAME, ObjectFactoryParser::parseObjectFactory)
            .orElse(null);
    }

}
