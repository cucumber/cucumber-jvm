package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.cucumber.core.options.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_LIMIT_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_ORDER_PROPERTY_NAME;
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
import static io.cucumber.core.options.OptionsFileParser.parseFeatureWithLinesFile;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class CucumberPropertiesParser {

    private static <T> Function<String, Collection<T>> splitAndMap(Function<String, T> parse) {
        return combined -> stream(combined.split(","))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .map(parse)
            .collect(toList());
    }

    private static <T> Function<String, Collection<T>> splitAndThenFlatMap(Function<String, Stream<T>> parse) {
        return combined -> stream(combined.split(","))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .flatMap(parse)
            .collect(toList());
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
            EXECUTION_STRICT_PROPERTY_NAME,
            Boolean::parseBoolean,
            builder::setStrict
        );

        parseAll(properties,
            FEATURES_PROPERTY_NAME,
            splitAndThenFlatMap(s -> parseFeatureOrRerunFile(builder, s)),
            builder::addFeature
        );

        parse(properties,
            FILTER_NAME_PROPERTY_NAME,
            Pattern::compile,
            builder::addNameFilter
        );

        parse(properties,
            FILTER_TAGS_PROPERTY_NAME,
            Function.identity(),
            builder::addTagFilter
        );

        parseAll(properties,
            GLUE_PROPERTY_NAME,
            splitAndMap(GluePath::parse),
            builder::addGlue
        );

        parse(properties,
            OBJECT_FACTORY_PROPERTY_NAME,
            ObjectFactoryParser::parseObjectFactory,
            builder::setObjectFactoryClass
        );

        parseAll(properties,
            PLUGIN_PROPERTY_NAME,
            splitAndMap(Function.identity()),
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

    private Stream<FeatureWithLines> parseFeatureOrRerunFile(RuntimeOptionsBuilder builder, String property) {
        if(property.startsWith("@")){
            return parseRerunFile(builder, property.substring(1)).stream();
        }
        return Stream.of(FeatureWithLines.parse(property));
    }

    private Collection<FeatureWithLines> parseRerunFile(RuntimeOptionsBuilder builder, String property) {
        builder.setIsRerun(true);
        Path rerunFile = Paths.get(property);
        return parseFeatureWithLinesFile(rerunFile);
    }

    private <T> void parseAll(Map<String, String> properties, String propertyName, Function<String, Collection<T>> parser, Consumer<T> setter) {
        String property = properties.get(propertyName);
        if (property == null || property.isEmpty()) {
            return;
        }
        try {
            Collection<T> parsed = parser.apply(property);
            parsed.forEach(setter);
        } catch (Exception e) {
            throw new CucumberException("Failed to parse '" + propertyName + "' with value '" + property + "'", e);
        }
    }

    private <T> void parse(Map<String, String> properties, String propertyName, Function<String, T> parser, Consumer<T> setter) {
        parseAll(properties, propertyName, parser.andThen(Collections::singletonList), setter);
    }

    private RuntimeOptionsBuilder parseCucumberOptions(String cucumberOptions) {
        RuntimeOptionsBuilder builder;
        RuntimeOptionsParser parser = new RuntimeOptionsParser();
        List<String> args = ShellWords.parse(cucumberOptions);
        builder = parser.parse(args);
        return builder;
    }

}
