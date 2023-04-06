package io.cucumber.junit.platform.engine;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.ObjectFactoryParser;
import io.cucumber.core.options.PluginOption;
import io.cucumber.core.options.SnippetTypeParser;
import io.cucumber.core.options.UuidGeneratorParser;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.UUID_GENERATOR_PROPERTY_NAME;

class CucumberEngineOptions implements
        io.cucumber.core.plugin.Options,
        io.cucumber.core.runner.Options,
        io.cucumber.core.backend.Options,
        io.cucumber.core.eventbus.Options {

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

        getPublishPlugin()
                .ifPresent(plugins::add);

        return plugins;
    }

    private Optional<PluginOption> getPublishPlugin() {
        Optional<PluginOption> fromToken = getPublishTokenPlugin();
        Optional<PluginOption> fromEnabled = getPublishEnabledPlugin();

        Optional<PluginOption> plugin = Stream.of(fromToken, fromEnabled)
                .flatMap(pluginOption -> pluginOption.map(Stream::of).orElseGet(Stream::empty))
                .findFirst();

        // With higher java version use ifPresentOrElse in plugins()
        if (plugin.isPresent()) {
            return plugin;
        }
        return getPublishQuietPlugin();
    }

    private Optional<PluginOption> getPublishQuietPlugin() {
        Optional<PluginOption> noPublishOption = Optional.of(PluginOption.forClass(NoPublishFormatter.class));
        Optional<PluginOption> quiteOption = Optional.empty();
        return configurationParameters
                .getBoolean(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME)
                .map(quite -> quite ? quiteOption : noPublishOption)
                .orElse(noPublishOption);
    }

    private Optional<PluginOption> getPublishTokenPlugin() {
        return configurationParameters
                .get(PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME)
                .map(token -> PluginOption.forClass(PublishFormatter.class, token));
    }

    private Optional<PluginOption> getPublishEnabledPlugin() {
        Optional<PluginOption> enabledOption = Optional.of(PluginOption.forClass(PublishFormatter.class));
        Optional<PluginOption> disabledOption = Optional.empty();
        return configurationParameters
                .getBoolean(PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME)
                .flatMap(enabled -> enabled ? enabledOption : disabledOption);
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

    @Override
    public Class<? extends UuidGenerator> getUuidGeneratorClass() {
        return configurationParameters
                .get(UUID_GENERATOR_PROPERTY_NAME, UuidGeneratorParser::parseUuidGenerator)
                .orElse(null);
    }

    boolean isParallelExecutionEnabled() {
        return configurationParameters
                .getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME)
                .orElse(false);
    }

    NamingStrategy namingStrategy() {
        return configurationParameters
                .get(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, DefaultNamingStrategy::getStrategy)
                .orElse(DefaultNamingStrategy.SHORT);
    }

    List<FeatureWithLines> featuresWithLines() {
        return configurationParameters.get(FEATURES_PROPERTY_NAME,
            s -> Arrays.stream(s.split(","))
                    .map(String::trim)
                    .map(FeatureWithLines::parse)
                    .sorted(Comparator.comparing(FeatureWithLines::uri))
                    .distinct()
                    .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
