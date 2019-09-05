package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.order.StandardPickleOrders;
import io.cucumber.core.snippets.SnippetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CucumberPropertiesParserTest {

    private final CucumberPropertiesParser cucumberPropertiesParser = new CucumberPropertiesParser();
    private final Map<String, String> properties = new HashMap<>();

    @TempDir
    Path temp;

    @Test
    void should_parse_cucumber_options() {
        properties.put(Constants.OPTIONS_PROPERTY_NAME, "--glue com.example");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getGlue(), equalTo(singletonList(URI.create("classpath:com/example"))));
    }

    @Test
    void should_parse_ansi_colors() {
        properties.put(Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME, "true");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.isMonochrome(), equalTo(true));
    }

    @Test
    void should_parse_dry_run() {
        properties.put(Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, "true");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.isDryRun(), equalTo(true));
    }

    @Test
    void should_parse_execution_order() {
        properties.put(Constants.EXECUTION_ORDER_PROPERTY_NAME, "reverse");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getPickleOrder(), equalTo(StandardPickleOrders.reverseLexicalUriOrder()));
    }

    @Test
    void should_parse_parallel() {
        properties.put(Constants.EXECUTION_PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, "12");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getThreads(), equalTo(12));
    }

    @Test
    void should_parse_strict() {
        properties.put(Constants.EXECUTION_STRICT_PROPERTY_NAME, "true");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.isStrict(), equalTo(true));
    }

    @Test
    void should_parse_features() {
        properties.put(Constants.FEATURES_PROPERTY_NAME, "com/example.feature");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getFeaturePaths(), contains(
            URI.create("file:com/example.feature")
        ));
    }

    @Test
    void should_parse_features_list() {
        properties.put(Constants.FEATURES_PROPERTY_NAME, "com/example/app.feature, com/example/other.feature");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getFeaturePaths(), contains(
            URI.create("file:com/example/app.feature"),
            URI.create("file:com/example/other.feature")
        ));
    }

    @Test
    void should_parse_filter_name() {
        properties.put(Constants.FILTER_NAME_PROPERTY_NAME, "Test.*");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getNameFilters().get(0).pattern(), equalTo(
            "Test.*"
        ));
    }

    @Test
    void should_parse_filter_tag() {
        properties.put(Constants.FILTER_TAGS_PROPERTY_NAME, "@No and not @Never");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getTagExpressions(), contains(
            "@No and not @Never"
        ));
    }

    @Test
    void should_parse_glue() {
        properties.put(Constants.GLUE_PROPERTY_NAME, "com.example.steps");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getGlue(), contains(
            URI.create("classpath:com/example/steps")
        ));
    }

    @Test
    void should_parse_glue_list() {
        properties.put(Constants.GLUE_PROPERTY_NAME, "com.example.app.steps, com.example.other.steps");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getGlue(), contains(
            URI.create("classpath:com/example/app/steps"),
            URI.create("classpath:com/example/other/steps")
        ));
    }

    @Test
    void should_parse_object_factory() {
        properties.put(Constants.OBJECT_FACTORY_PROPERTY_NAME, CustomObjectFactory.class.getName());
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getObjectFactoryClass(), equalTo(CustomObjectFactory.class));
    }

    @Test
    void should_parse_plugin() {
        properties.put(Constants.PLUGIN_PROPERTY_NAME, "json:target/cucumber.json, html:target/cucumber.html");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.plugins().get(0).pluginString(), equalTo("json:target/cucumber.json"));
        assertThat(options.plugins().get(1).pluginString(), equalTo("html:target/cucumber.html"));
    }

    @Test
    void should_parse_snippet_type() {
        properties.put(Constants.SNIPPET_TYPE_PROPERTY_NAME, "camelcase");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getSnippetType(), equalTo(SnippetType.CAMELCASE));
    }

    @Test
    void should_parse_wip() {
        properties.put(Constants.WIP_PROPERTY_NAME, "true");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.isWip(), equalTo(true));
    }

    @Test
    void should_throw_when_fails_to_parse() {
        properties.put(Constants.OBJECT_FACTORY_PROPERTY_NAME, "garbage");
        CucumberException exception = assertThrows(
            CucumberException.class,
            () -> cucumberPropertiesParser.parse(properties).build()
        );
        assertThat(exception.getMessage(), equalTo("Failed to parse 'cucumber.object-factory' with value 'garbage'"));
    }

    @Test
    void should_parse_rerun_file() throws IOException {
        Path path = mockFileResource("path/to.feature");
        properties.put(Constants.RERUN_FILE_PROPERTY_NAME, path.toString());
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getFeaturePaths(), containsInAnyOrder(URI.create("file:path/to.feature")));
    }

    private Path mockFileResource(String... contents) throws IOException {
        Path path = Files.createTempFile(temp, "", ".txt");
        Files.write(path, Arrays.asList(contents), UTF_8, WRITE);
        return path;
    }

    private static final class CustomObjectFactory implements ObjectFactory {
        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }

}
