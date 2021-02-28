package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.EventPublisher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.resource.ClasspathSupport.rootPackageUri;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CommandlineOptionsParserTest {

    private final Map<String, String> properties = new HashMap<>();
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final CommandlineOptionsParser parser = new CommandlineOptionsParser(out);

    @Test
    void testParseWithObjectFactoryArgument() {
        RuntimeOptionsBuilder optionsBuilder = parser.parse("--object-factory", TestObjectFactory.class.getName());
        assertNotNull(optionsBuilder);
        RuntimeOptions options = optionsBuilder.build();
        assertNotNull(options);
        assertThat(options.getObjectFactoryClass(), Is.is(equalTo(TestObjectFactory.class)));
    }

    @Test
    void has_version_from_properties_file() {
        parser.parse("--version");
        assertThat(output(), matchesPattern("\\d+\\.\\d+\\.\\d+(-RC\\d+)?(-SNAPSHOT)?\r?\n"));
        assertThat(parser.exitStatus(), is(Optional.of((byte) 0x0)));
    }

    private String output() {
        return new String(out.toByteArray());
    }

    @Test
    void prints_usage_for_unknown_options() {
        parser.parse("--not-an-option");
        assertThat(output(), startsWith("Unknown option: --not-an-option"));
        assertThat(parser.exitStatus(), is(Optional.of((byte) 0x1)));

    }

    @Test
    void prints_usage_for_help() {
        parser.parse("--help");

        assertThat(output(), startsWith("Usage"));
    }

    @Test
    void assigns_feature_paths() {
        RuntimeOptions options = parser
                .parse("somewhere_else")
                .build();
        assertThat(options.getFeaturePaths(), contains(new File("somewhere_else").toURI()));
    }

    @Test
    void strips_line_filters_from_feature_paths_and_put_them_among_line_filters() {
        RuntimeOptions options = parser
                .parse("somewhere_else.feature:3")
                .build();

        assertAll(
            () -> assertThat(options.getFeaturePaths(), contains(new File("somewhere_else.feature").toURI())),
            () -> assertThat(options.getLineFilters(),
                hasEntry(new File("somewhere_else.feature").toURI(), singleton(3))));
    }

    @Test
    void select_multiple_lines_in_a_features() {
        RuntimeOptions options = parser
                .parse("somewhere_else.feature:3:5")
                .build();
        assertThat(options.getFeaturePaths(), contains(new File("somewhere_else.feature").toURI()));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(new File("somewhere_else.feature").toURI(), lines));
    }

    @Test
    void combines_line_filters_from_repeated_features() {
        RuntimeOptions options = parser
                .parse("classpath:somewhere_else.feature:3", "classpath:somewhere_else.feature:5")
                .build();
        assertThat(options.getFeaturePaths(), contains(uri("classpath:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("classpath:somewhere_else.feature"), lines));
    }

    public static URI uri(String s) {
        return URI.create(s);
    }

    @Test
    void assigns_filters_from_tags() {
        RuntimeOptions options = parser
                .parse("--tags", "@keep_this")
                .build();

        List<String> tagExpressions = options.getTagExpressions().stream()
                .map(Object::toString)
                .collect(toList());

        assertThat(tagExpressions, contains("@keep_this"));
    }

    @Test
    void throws_runtime_exception_on_malformed_tag_expression() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            RuntimeOptions options = parser
                    .parse("--tags", ")")
                    .build();
        });
    }

    @Test
    void assigns_glue() {
        RuntimeOptions options = parser
                .parse("--glue", "somewhere")
                .build();
        assertThat(options.getGlue(), contains(uri("classpath:/somewhere")));
    }

    @Test
    void creates_html_formatter() {
        RuntimeOptions options = parser
                .parse("--plugin", "html:target/deeply/nested.html", "--glue", "somewhere")
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("io.cucumber.core.plugin.HtmlFormatter"));
    }

    @Test
    void creates_progress_formatter_as_default() {
        RuntimeOptions options = parser
                .parse()
                .addDefaultFormatterIfAbsent()
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("io.cucumber.core.plugin.ProgressFormatter"));
    }

    @Test
    void creates_default_summary_printer_when_no_summary_printer_plugin_is_specified() {
        RuntimeOptions options = parser
                .parse("--plugin", "pretty")
                .addDefaultSummaryPrinterIfAbsent()
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter")));
    }

    private static Matcher<Plugin> plugin(final String pluginName) {
        return new TypeSafeDiagnosingMatcher<Plugin>() {
            @Override
            protected boolean matchesSafely(Plugin plugin, Description description) {
                description.appendValue(plugin.getClass().getName());
                return plugin.getClass().getName().equals(pluginName);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(pluginName);
            }
        };
    }

    @Test
    void creates_null_summary_printer() {
        RuntimeOptions options = parser
                .parse("--plugin", "null_summary", "--glue", "somewhere")
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertAll(
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.NullSummaryPrinter"))),
            () -> assertThat(plugins.getPlugins(),
                not(hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter")))));
    }

    @Test
    void replaces_incompatible_intellij_idea_plugin() {
        RuntimeOptions options = parser
                .parse("--plugin", "org.jetbrains.plugins.cucumber.java.run.CucumberJvm3SMFormatter")
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertThat(plugins.getPlugins(), not(hasItem(plugin("io.cucumber.core.plugin.PrettyPrinter"))));
    }

    @Test
    void assigns_wip() {
        RuntimeOptions options = parser
                .parse("--wip")
                .build();
        assertThat(options.isWip(), is(true));
    }

    @Test
    void assigns_wip_short() {
        RuntimeOptions options = parser
                .parse("-w")
                .build();
        assertThat(options.isWip(), is(true));
    }

    @Test
    void default_wip() {
        RuntimeOptions options = parser
                .parse()
                .build();
        assertThat(options.isWip(), is(false));
    }

    @Test
    void name_without_spaces_is_preserved() {
        RuntimeOptions options = parser
                .parse("--name", "someName")
                .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("someName"));
    }

    @Test
    void name_with_spaces_is_preserved() {
        RuntimeOptions options = parser
                .parse("--name", "some Name")
                .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    void combines_tag_filters_from_env_if_rerun_file_specified_in_cli() {
        RuntimeOptions runtimeOptions = parser
                .parse("@src/test/resources/io/cucumber/core/options/runtime-options-rerun.txt")
                .build();

        RuntimeOptions options = new CucumberPropertiesParser()
                .parse(singletonMap(FILTER_TAGS_PROPERTY_NAME, "@should_not_be_clobbered"))
                .build(runtimeOptions);

        List<String> actual = options.getTagExpressions().stream()
                .map(e -> e.toString())
                .collect(toList());

        assertAll(
            () -> assertThat(actual, contains("@should_not_be_clobbered")),
            () -> assertThat(options.getLineFilters(),
                hasEntry(new File("this/should/be/rerun.feature").toURI(), singleton(12))));
    }

    @Test
    void clobbers_line_filters_from_cli_if_tags_are_specified_in_env() {
        RuntimeOptions runtimeOptions = parser
                .parse("file:path/to.feature")
                .build();

        RuntimeOptions options = new CucumberPropertiesParser()
                .parse(singletonMap(FILTER_TAGS_PROPERTY_NAME, "@should_not_be_clobbered"))
                .build(runtimeOptions);

        List<String> actual = options.getTagExpressions().stream()
                .map(e -> e.toString())
                .collect(toList());

        assertAll(
            () -> assertThat(actual, contains("@should_not_be_clobbered")),
            () -> assertThat(options.getLineFilters(), is(emptyMap())),
            () -> assertThat(options.getFeaturePaths(), contains(new File("path/to.feature").toURI())));
    }

    @Test
    void fail_on_unsupported_options() {
        parser
                .parse("-concreteUnsupportedOption", "somewhere", "somewhere_else")
                .build();
        assertThat(output(), startsWith("Unknown option: -concreteUnsupportedOption"));
        assertThat(parser.exitStatus(), is(Optional.of((byte) 0x1)));
    }

    @Test
    void threads_default_1() {
        RuntimeOptions options = parser
                .parse()
                .build();
        assertThat(options.getThreads(), is(1));
    }

    @Test
    void ensure_threads_param_is_used() {
        RuntimeOptions options = parser
                .parse("--threads", "10")
                .build();
        assertThat(options.getThreads(), is(10));
    }

    @Test
    void ensure_less_than_1_thread_is_not_allowed() {
        parser
                .parse("--threads", "0")
                .build();
        assertThat(output(), equalToCompressingWhiteSpace("--threads must be > 0"));
        assertThat(parser.exitStatus(), is(Optional.of((byte) 0x1)));
    }

    @Test
    void set_monochrome_on_color_aware_formatters() {
        RuntimeOptions options = parser
                .parse("--monochrome", "--plugin", AwareFormatter.class.getName())
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isMonochrome(), is(true));
    }

    @Test
    void set_strict_on_strict_aware_formatters() {
        RuntimeOptions options = parser
                .parse("--strict", "--plugin", AwareFormatter.class.getName())
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isStrict(), is(true));

    }

    @Test
    void ensure_default_snippet_type_is_underscore() {
        RuntimeOptions runtimeOptions = parser
                .parse()
                .build();
        RuntimeOptions options = new CucumberPropertiesParser()
                .parse(properties)
                .build(runtimeOptions);
        assertThat(options.getSnippetType(), is(SnippetType.UNDERSCORE));
    }

    @Test
    void order_type_default_none() {
        RuntimeOptions options = parser
                .parse()
                .build();
        Pickle a = createPickle("file:path/file1.feature", "a");
        Pickle b = createPickle("file:path/file2.feature", "b");
        assertThat(options.getPickleOrder()
                .orderPickles(Arrays.asList(a, b)),
            contains(a, b));
    }

    private Pickle createPickle(String uri, String name) {
        Feature feature = TestFeatureParser.parse(uri, "" +
                "Feature: Test feature\n" +
                "  Scenario: " + name + "\n" +
                "     Given I have 4 cukes in my belly\n");
        return feature.getPickles().get(0);
    }

    @Test
    void ensure_order_type_reverse_is_used() {
        RuntimeOptions options = parser
                .parse("--order", "reverse")
                .build();
        Pickle a = createPickle("file:path/file1.feature", "a");
        Pickle b = createPickle("file:path/file2.feature", "b");
        assertThat(options.getPickleOrder()
                .orderPickles(Arrays.asList(a, b)),
            contains(b, a));
    }

    @Test
    void ensure_order_type_random_is_used() {
        parser
                .parse("--order", "random")
                .build();
    }

    @Test
    void ensure_order_type_random_with_seed_is_used() {
        RuntimeOptions options = parser
                .parse("--order", "random:5000")
                .build();
        Pickle a = createPickle("file:path/file1.feature", "a");
        Pickle b = createPickle("file:path/file2.feature", "b");
        Pickle c = createPickle("file:path/file3.feature", "c");
        assertThat(options.getPickleOrder()
                .orderPickles(Arrays.asList(a, b, c)),
            contains(c, a, b));
    }

    @Test
    void ensure_invalid_ordertype_is_not_allowed() {
        Executable testMethod = () -> parser
                .parse("--order", "invalid")
                .build();
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(actualThrown.getMessage(),
            is(equalTo("Invalid order. Must be either reverse, random or random:<long>")));
    }

    @Test
    void ensure_less_than_1_count_is_not_allowed() {
        parser
                .parse("--count", "0")
                .build();
        assertThat(output(), equalToCompressingWhiteSpace("--count must be > 0"));
        assertThat(parser.exitStatus(), is(Optional.of((byte) 0x1)));
    }

    @Test
    void scans_class_path_root_for_glue_by_default() {
        RuntimeOptions options = parser
                .parse()
                .addDefaultGlueIfAbsent()
                .build();
        assertThat(options.getGlue(), is(singletonList(rootPackageUri())));
    }

    @Test
    void scans_class_path_root_for_features_by_default() {
        RuntimeOptions options = parser
                .parse()
                .addDefaultFeaturePathIfAbsent()
                .build();
        assertThat(options.getFeaturePaths(), is(singletonList(rootPackageUri())));
        assertThat(options.getLineFilters(), is(emptyMap()));
    }

    private static final class TestObjectFactory implements ObjectFactory {

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

    public static final class AwareFormatter implements StrictAware, ColorAware, EventListener {

        private boolean strict;
        private boolean monochrome;

        private boolean isStrict() {
            return strict;
        }

        @Override
        public void setStrict(boolean strict) {
            this.strict = strict;
        }

        boolean isMonochrome() {
            return monochrome;
        }

        @Override
        public void setMonochrome(boolean monochrome) {
            this.monochrome = monochrome;
        }

        @Override
        public void setEventPublisher(EventPublisher publisher) {

        }

    }

}
