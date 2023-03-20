package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.cucumber.core.options.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_LIMIT_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.EXECUTION_ORDER_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.OPTIONS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.WIP_PROPERTY_NAME;
import static io.cucumber.core.options.OptionsFileParser.parseFeatureWithLinesFile;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public final class CucumberPropertiesParser {

    private static final Logger log = LoggerFactory.getLogger(CucumberPropertiesParser.class);

    public RuntimeOptionsBuilder parse(Map<String, String> properties) {
        return parse(properties::get);
    }

    public RuntimeOptionsBuilder parse(CucumberPropertiesProvider properties) {
        RuntimeOptionsBuilder builder = new RuntimeOptionsBuilder();

        parse(properties,
            ANSI_COLORS_DISABLED_PROPERTY_NAME,
            BooleanString::parseBoolean,
            builder::setMonochrome);

        parse(properties,
            EXECUTION_DRY_RUN_PROPERTY_NAME,
            BooleanString::parseBoolean,
            builder::setDryRun);

        parse(properties,
            EXECUTION_LIMIT_PROPERTY_NAME,
            Integer::parseInt,
            builder::setCount);

        parse(properties,
            EXECUTION_ORDER_PROPERTY_NAME,
            PickleOrderParser::parse,
            builder::setPickleOrder);

        parseAll(properties,
            FEATURES_PROPERTY_NAME,
            splitAndThenFlatMap(CucumberPropertiesParser::parseFeatureFile),
            builder::addFeature);

        parseAll(properties,
            // For historical reasons rerun files are also provided through the
            // feature property. They are differentiated by prefixing the uri
            // name with an `@` symbol.
            FEATURES_PROPERTY_NAME,
            splitAndThenFlatMap(CucumberPropertiesParser::parseRerunFile),
            builder::addRerun);

        parse(properties,
            FILTER_NAME_PROPERTY_NAME,
            Pattern::compile,
            builder::addNameFilter);

        parse(properties,
            FILTER_TAGS_PROPERTY_NAME,
            TagExpressionParser::parse,
            builder::addTagFilter);

        parseAll(properties,
            GLUE_PROPERTY_NAME,
            splitAndMap(GluePath::parse),
            builder::addGlue);

        parse(properties,
            OBJECT_FACTORY_PROPERTY_NAME,
            ObjectFactoryParser::parseObjectFactory,
            builder::setObjectFactoryClass);

        parse(properties,
            OPTIONS_PROPERTY_NAME,
            identity(),
            warnWhenCucumberOptionsIsUsed());

        parseAll(properties,
            PLUGIN_PROPERTY_NAME,
            splitAndMap(identity()),
            builder::addPluginName);

        parse(properties,
            PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME,
            identity(), // No validation - validated on server
            builder::setPublishToken);

        parse(properties,
            PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME,
            BooleanString::parseBoolean,
            builder::setPublish);

        parse(properties,
            PLUGIN_PUBLISH_QUIET_PROPERTY_NAME,
            BooleanString::parseBoolean,
            builder::setPublishQuiet);

        parse(properties,
            SNIPPET_TYPE_PROPERTY_NAME,
            SnippetTypeParser::parseSnippetType,
            builder::setSnippetType);

        parse(properties,
            WIP_PROPERTY_NAME,
            BooleanString::parseBoolean,
            builder::setWip);

        return builder;
    }

    private static Consumer<String> warnWhenCucumberOptionsIsUsed() {
        // Quite a few old blogs still recommend the use of cucumber.options
        // This should take care of recurring question involving this property.
        return commandLineOptions -> log.warn(() -> String.format("" +
                "Passing commandline options via the property '%s' is no longer supported. " +
                "Please use individual properties instead. " +
                "See the java doc on %s for details.",
            OPTIONS_PROPERTY_NAME, Constants.class.getName()));
    }

    private <T> void parse(
            CucumberPropertiesProvider properties, String propertyName, Function<String, T> parser, Consumer<T> setter
    ) {
        parseAll(properties, propertyName, parser.andThen(Collections::singletonList), setter);
    }

    private <T> void parseAll(
            CucumberPropertiesProvider properties, String propertyName, Function<String, Collection<T>> parser,
            Consumer<T> setter
    ) {
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

    private static <T> Function<String, Collection<T>> splitAndThenFlatMap(Function<String, Stream<T>> parse) {
        return combined -> stream(combined.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .flatMap(parse)
                .collect(toList());
    }

    private static Stream<FeatureWithLines> parseFeatureFile(String property) {
        if (property.startsWith("@")) {
            return Stream.empty();
        }
        return Stream.of(FeatureWithLines.parse(property));
    }

    private static <T> Function<String, Collection<T>> splitAndMap(Function<String, T> parse) {
        return combined -> stream(combined.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(parse)
                .collect(toList());
    }

    private Stream<Collection<FeatureWithLines>> parseRerunFiles(String property) {
        if (property.startsWith("@")) {
            String pathStr = property.substring(1);
            File filePath = new File(pathStr);
            if (filePath.isDirectory()) { // path is a directory
                if (filePath.list() != null && filePath.list().length > 0) {
                    // parse rerun files here

                } else {
                    return Stream.empty(); // no files in folder
                }
            } else { // path is a file
                return Stream.of(parseFeatureWithLinesFile(Paths.get(pathStr))); // parse
                // file
            }
        }
        return Stream.empty();
    }

    private static Stream<Collection<FeatureWithLines>> parseRerunFile(String property) {
        if (property.startsWith("@")) {
            Path rerunFile = Paths.get(property.substring(1));
            return Stream.of(parseFeatureWithLinesFile(rerunFile));
        }
        return Stream.empty();
    }

}
