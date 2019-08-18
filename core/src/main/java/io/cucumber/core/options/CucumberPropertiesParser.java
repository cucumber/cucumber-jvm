package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.cucumber.core.options.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_LIMIT_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_ORDER_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_STRICT_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.OPTIONS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.WIP_PROPERTY_NAME;

public final class CucumberPropertiesParser {

    private final ResourceLoader resourceLoader;

    public CucumberPropertiesParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public CucumberPropertiesParser() {
        this(new MultiLoader(CucumberPropertiesParser.class.getClassLoader()));
    }

    public RuntimeOptionsBuilder parse(Map<String, String> properties) {
        final RuntimeOptionsBuilder builder;
        String cucumberOptions = properties.get(OPTIONS_PROPERTY_NAME);
        if (cucumberOptions != null) {
            builder = parseCucumberOptions(cucumberOptions);
        } else {
            builder = new RuntimeOptionsBuilder();
        }

        parse(properties,
            ANSI_COLORS_DISABLED_PROPERTY_NAME,
            Boolean::parseBoolean,
            builder::setMonochrome
        );

        parse(properties,
            EXECUTION_DRY_RUN_PROPERTY_NAME,
            Boolean::parseBoolean,
            builder::setDryRun
        );

        parse(properties,
            EXECUTION_LIMIT_PROPERTY_NAME,
            Integer::parseInt,
            builder::setCount
        );

        parse(properties,
            EXECUTION_ORDER_PROPERTY_NAME,
            PickleOrderParser::parse,
            builder::setPickleOrder
        );

        parse(properties,
            EXECUTION_PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME,
            Integer::parseInt,
            builder::setThreads
        );

        parse(properties,
            EXECUTION_STRICT_PROPERTY_NAME,
            Boolean::parseBoolean,
            builder::setStrict
        );

        parse(properties,
            FEATURES_PROPERTY_NAME,
            FeatureWithLines::parse,
            builder::addFeature
        );

        parse(properties,
            FILTER_NAME_PROPERTY_NAME,
            Pattern::compile,
            builder::addNameFilter
        );

        parse(properties,
            FILTER_TAGS_PROPERTY_NAME,
            //TODO: Parse early
            Function.identity(),
            builder::addTagFilter
        );

        parse(properties,
            GLUE_PROPERTY_NAME,
            GluePath::parse,
            builder::addGlue
        );

        parse(properties,
            OBJECT_FACTORY_PROPERTY_NAME,
            ObjectFactoryParser::parseObjectFactory,
            builder::setObjectFactoryClass
        );

        parse(properties,
            PLUGIN_PROPERTY_NAME,
            //TODO: Parse early
            Function.identity(),
            plugin -> builder.addPluginName(plugin, true)
        );

        parse(properties,
            SNIPPET_TYPE_PROPERTY_NAME,
            SnippetTypeParser::parseSnippetType,
            builder::setSnippetType
        );
        parse(properties,
            WIP_PROPERTY_NAME,
            Boolean::parseBoolean,
            builder::setWip
        );

        return builder;
    }

    private <T> void parse(Map<String, String> properties, String propertyName, Function<String, T> parser, Consumer<T> setter) {
        String property = properties.get(propertyName);
        if (property == null) {
            return;
        }
        try {
            T parsed = parser.apply(property);
            setter.accept(parsed);
        } catch (Exception e) {
            throw new CucumberException("Failed to parse '" + propertyName + "' with value '" + property + "'", e);
        }

    }

    private RuntimeOptionsBuilder parseCucumberOptions(String cucumberOptions) {
        RuntimeOptionsBuilder builder;
        RerunLoader rerunLoader = new RerunLoader(resourceLoader);
        RuntimeOptionsParser parser = new RuntimeOptionsParser(rerunLoader);
        List<String> args = ShellWords.parse(cucumberOptions);
        builder = parser.parse(args);
        return builder;
    }

}
