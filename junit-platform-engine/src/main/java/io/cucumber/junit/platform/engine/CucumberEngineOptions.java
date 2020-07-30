package io.cucumber.junit.platform.engine;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.ObjectFactoryParser;
import io.cucumber.core.options.PluginOption;
import io.cucumber.core.options.PublishTokenParser;
import io.cucumber.core.options.SnippetTypeParser;
import io.cucumber.core.plugin.NoPublishFormatter;
import io.cucumber.core.plugin.PublishFormatter;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.junit.platform.engine.ConfigurationParameters;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME;
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
    public List<Plugin> plugins() {
        List<Plugin> plugins = configurationParameters.get(PLUGIN_PROPERTY_NAME, s -> Arrays.stream(s.split(","))
                .map(String::trim)
                .map(PluginOption::parse)
                .map(pluginOption -> (Plugin) pluginOption)
                .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);

        plugins.add(getPublishPlugin());

        return plugins;
    }

    private PluginOption getPublishPlugin() {
        String publishToken = configurationParameters
                .get(PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME)
                .map(PublishTokenParser::parse)
                .orElse(null);

        boolean publish = configurationParameters
                .getBoolean(PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME)
                .orElse(Boolean.FALSE);

        Boolean publishQuiet = configurationParameters
                .getBoolean(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME)
                .orElse(Boolean.FALSE);

        if (publishToken != null) {
            return PluginOption.forClass(PublishFormatter.class, publishToken);
        }
        return publish ? PluginOption.forClass(PublishFormatter.class)
                : PluginOption.forClass(NoPublishFormatter.class, publishQuiet.toString());

    }

    @Override
    public boolean isMonochrome() {
        return configurationParameters
                .getBoolean(ANSI_COLORS_DISABLED_PROPERTY_NAME)
                .orElse(false);
    }

    @Override
    public boolean isWip() {
        return false;
    }

    Optional<Expression> tagFilter() {
        return configurationParameters.get(FILTER_TAGS_PROPERTY_NAME, TagExpressionParser::parse);
    }

    Optional<Pattern> nameFilter() {
        return configurationParameters.get(FILTER_NAME_PROPERTY_NAME, Pattern::compile);
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

    boolean isParallelExecutionEnabled() {
        return configurationParameters
                .get(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, Boolean::parseBoolean)
                .orElse(false);
    }

}
