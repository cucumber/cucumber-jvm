package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.EventPublisher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.OPTIONS_PROPERTY_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RuntimeOptionsTest {

    private final Map<String, String> properties = new HashMap<>();

    public static URI uri(String s) {
        return URI.create(s);
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
    void has_version_from_properties_file() {
        assertTrue(RuntimeOptionsParser.VERSION.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
    }

    @Test
    void has_usage() {
        RuntimeOptionsParser.loadUsageTextIfNeeded();
        assertThat(RuntimeOptionsParser.usageText, startsWith("Usage"));
    }

    @Test
    void assigns_feature_paths() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else")));
    }

    @Test
    void strips_line_filters_from_feature_paths_and_put_them_among_line_filters() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else.feature:3")
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature"))),
            () -> assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), singleton(3)))
        );
    }

    @Test
    void select_multiple_lines_in_a_features() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else.feature:3:5")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), lines));
    }


    @Test
    void combines_line_filters_from_repeated_features() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else.feature:3", "somewhere_else.feature:5")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), lines));
    }

    @Test
    void assigns_filters_from_tags() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--tags", "@keep_this")
            .build();
        assertThat(options.getTagExpressions(), contains("@keep_this"));
    }

    @Test
    void trims_options() {
        assertThat(ShellWords.parse("  --glue  somewhere   somewhere_else"),
            contains("--glue", "somewhere", "somewhere_else"));
    }

    @Test
    void assigns_glue() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--glue", "somewhere")
            .build();
        assertThat(options.getGlue(), contains(uri("classpath:somewhere")));
    }

    @Test
    void creates_html_formatter() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "html:target/some/dir", "--glue", "somewhere")
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(ClockStub.systemUTC()));

        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("io.cucumber.core.plugin.HTMLFormatter"));
    }

    @Test
    void creates_progress_formatter_as_default() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse()
            .addDefaultFormatterIfNotPresent()
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("io.cucumber.core.plugin.ProgressFormatter"));
    }

    @Test
    void creates_default_summary_printer_when_no_summary_printer_plugin_is_specified() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "pretty")
            .addDefaultSummaryPrinterIfNotPresent()
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter")));
    }

    @Test
    void creates_null_summary_printer() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "null_summary", "--glue", "somewhere")
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertAll("Checking Plugins",
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.NullSummaryPrinter"))),
            () -> assertThat(plugins.getPlugins(), not(hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter"))))
        );
    }

    @Test
    void replaces_incompatible_intellij_idea_plugin() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "org.jetbrains.plugins.cucumber.java.run.CucumberJvm3SMFormatter")
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertThat(plugins.getPlugins(), not(hasItem(plugin("io.cucumber.core.plugin.PrettyPrinter"))));
    }

    @Test
    void assigns_strict() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--strict")
            .build();
        assertTrue(options.isStrict());
    }

    @Test
    void assigns_strict_short() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("-s")
            .build();
        assertTrue(options.isStrict());
    }

    @Test
    void default_strict() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse()
            .build();
        assertThat(options.isStrict(), is(false));
    }

    @Test
    void assigns_wip() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--wip")
            .build();
        assertThat(options.isWip(), is(true));
    }

    @Test
    void assigns_wip_short() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("-w")
            .build();
        assertThat(options.isWip(), is(true));
    }

    @Test
    void default_wip() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse()
            .build();
        assertThat(options.isWip(), is(false));
    }

    @Test
    void name_without_spaces_is_preserved() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--name", "someName")
            .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("someName"));
    }

    @Test
    void name_with_spaces_is_preserved() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--name", "some Name")
            .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    void ensure_name_with_spaces_works_with_cucumber_options() {
        properties.put(OPTIONS_PROPERTY_NAME, "--name 'some Name'");
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    void ensure_name_with_spaces_works_with_args() {
        assertThat(ShellWords.parse("--name 'some Name'"), contains("--name", "some Name"));
    }

    @Test
    void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {
        properties.put(OPTIONS_PROPERTY_NAME, "--glue lookatme this_clobbers_feature_paths");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--strict", "--glue", "somewhere", "somewhere_else")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getFeaturePaths(), contains(uri("file:this_clobbers_feature_paths"))),
            () -> assertThat(options.getGlue(), contains(uri("classpath:lookatme"))),
            () -> assertTrue(options.isStrict())
        );
    }

    @Test
    void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        properties.put(OPTIONS_PROPERTY_NAME, "--tags @foo");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--glue", "somewhere"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getGlue(), contains(uri("classpath:somewhere")));
    }

    @Test
    void clobbers_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--tags @clobber_with_this");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--tags", "@should_be_clobbered"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getTagExpressions(), contains("@clobber_with_this"));
    }

    @Test
    void clobbers_tag_and_name_filters_from_cli_if_line_filters_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "path/file.feature:3");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--tags", "@should_be_clobbered", "--name", "should_be_clobbered")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getTagExpressions(), emptyCollectionOf(String.class));
    }

    @Test
    void clobbers_tag_and_name_filters_from_cli_if_rerun_file_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "@src/test/resources/io/cucumber/core/options/runtime-options-rerun.txt");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--tags", "@should_be_clobbered", "--name", "should_be_clobbered")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getTagExpressions(), emptyCollectionOf(String.class)),
            () -> assertThat(options.getLineFilters(), hasEntry(uri("file:this/should/be/rerun.feature"), singleton(12)))
        );
    }

    @Test
    void combines_tag_filters_from_env_if_rerun_file_specified_in_cli() {
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@src/test/resources/io/cucumber/core/options/runtime-options-rerun.txt")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(singletonMap(FILTER_TAGS_PROPERTY_NAME, "@should_not_be_clobbered"))
            .build(runtimeOptions);

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getTagExpressions(), contains("@should_not_be_clobbered")),
            () -> assertThat(options.getLineFilters(), hasEntry(uri("file:this/should/be/rerun.feature"), singleton(12)))
        );
    }

    @Test
    void clobbers_line_filters_from_cli_if_tags_are_specified_in_env() {
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("file:path/to.feature")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(singletonMap(FILTER_TAGS_PROPERTY_NAME, "@should_not_be_clobbered"))
            .build(runtimeOptions);

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getTagExpressions(), contains("@should_not_be_clobbered")),
            () -> assertThat(options.getLineFilters(), is(emptyMap())),
            () -> assertThat(options.getFeaturePaths(), contains(URI.create("file:path/to.feature")))
        );
    }

    @Test
    void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--strict");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--tags", "@keep_this"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getTagExpressions(), contains("@keep_this"));
    }

    @Test
    void clobbers_features_from_cli_if_features_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "new newer");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("old", "older"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:new"), uri("file:newer")));
    }

    @Test
    void strips_lines_from_features_from_cli_if_filters_are_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--tags @Tag");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("path/file.feature:3")
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:path/file.feature")));
    }

    @Test
    void preserves_features_from_cli_if_features_not_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--plugin pretty");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("old", "older"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:old"), uri("file:older")));

    }

    @Test
    void clobbers_line_filters_from_cli_if_features_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "new newer");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--tags", "@keep_this", "path/file1.feature:1"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getFeaturePaths(), contains(uri("file:new"), uri("file:newer"))),
            () -> assertThat(options.getTagExpressions(), contains("@keep_this"))
        );
    }

    @Test
    void clobbers_formatter_plugins_from_cli_if_formatters_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--plugin pretty");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "html:target/some/dir", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertAll("Checking Plugins",
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.PrettyFormatter"))),
            () -> assertThat(plugins.getPlugins(), not(hasItem(plugin("io.cucumber.core.plugin.HTMLFormatter"))))
        );
    }

    @Test
    void adds_to_formatter_plugins_with_add_plugin_option() {
        properties.put(OPTIONS_PROPERTY_NAME, "--add-plugin pretty");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "html:target/some/dir", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertAll("Checking Plugins",
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.HTMLFormatter"))),
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.PrettyFormatter")))
        );
    }

    @Test
    void clobbers_summary_plugins_from_cli_if_summary_printer_specified_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--plugin default_summary");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "null_summary", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertAll("Checking Plugins",
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter"))),
            () -> assertThat(plugins.getPlugins(), not(hasItem(plugin("io.cucumber.core.plugin.NullSummaryPrinter"))))
        );
    }

    @Test
    void adds_to_summary_plugins_with_add_plugin_option() {
        properties.put(OPTIONS_PROPERTY_NAME, "--add-plugin default_summary");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "null_summary", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertAll("Checking Plugins",
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.NullSummaryPrinter"))),
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter")))
        );
    }

    @Test
    void does_not_clobber_plugins_of_different_type_when_specifying_plugins_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--plugin default_summary");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "pretty", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertAll("Checking Plugins",
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.PrettyFormatter"))),
            () -> assertThat(plugins.getPlugins(), hasItem(plugin("io.cucumber.core.plugin.DefaultSummaryPrinter")))
        );
    }

    @Test
    void allows_removal_of_strict_in_cucumber_options_property() {
        properties.put(OPTIONS_PROPERTY_NAME, "--no-strict");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--strict")
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.isStrict(), is(false));
    }

    @Test
    void fail_on_unsupported_options() {
        Executable testMethod = () -> new CommandlineOptionsParser()
            .parse(asList("-concreteUnsupportedOption", "somewhere", "somewhere_else"))
            .build();
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo("Unknown option: -concreteUnsupportedOption")));
    }

    @Test
    void threads_default_1() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse(Collections.emptyList())
            .build();
        assertThat(options.getThreads(), is(1));
    }

    @Test
    void ensure_threads_param_is_used() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--threads", "10")
            .build();
        assertThat(options.getThreads(), is(10));
    }

    @Test
    void ensure_less_than_1_thread_is_not_allowed() {
        Executable testMethod = () -> new CommandlineOptionsParser()
            .parse("--threads", "0")
            .build();
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo("--threads must be > 0")));
    }

    @Test
    void set_monochrome_on_color_aware_formatters() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--monochrome", "--plugin", AwareFormatter.class.getName())
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isMonochrome(), is(true));
    }

    @Test
    void set_strict_on_strict_aware_formatters() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--strict", "--plugin", AwareFormatter.class.getName())
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isStrict(), is(true));

    }

    @Test
    void ensure_default_snippet_type_is_underscore() {
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(Collections.emptyList())
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getSnippetType(), is(SnippetType.UNDERSCORE));
    }

    @Test
    void set_snippet_type() {
        properties.put(OPTIONS_PROPERTY_NAME, "--snippets camelcase");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(Collections.emptyList())
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getSnippetType(), is(SnippetType.CAMELCASE));
    }

    @Test
    void order_type_default_none() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse(Collections.emptyList())
            .build();
        CucumberPickle a = createPickle("file:path/file1.feature", "a");
        CucumberPickle b = createPickle("file:path/file2.feature", "b");
        assertThat(options.getPickleOrder()
            .orderPickles(Arrays.asList(a, b)), contains(a, b));
    }

    @Test
    void ensure_order_type_reverse_is_used() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--order", "reverse")
            .build();
        CucumberPickle a = createPickle("file:path/file1.feature", "a");
        CucumberPickle b = createPickle("file:path/file2.feature", "b");
        assertThat(options.getPickleOrder()
            .orderPickles(Arrays.asList(a, b)), contains(b, a));
    }

    @Test
    void ensure_order_type_random_is_used() {
        new CommandlineOptionsParser()
            .parse("--order", "random")
            .build();
    }

    @Test
    void ensure_order_type_random_with_seed_is_used() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--order", "random:5000")
            .build();
        CucumberPickle a = createPickle("file:path/file1.feature", "a");
        CucumberPickle b = createPickle("file:path/file2.feature", "b");
        CucumberPickle c = createPickle("file:path/file3.feature", "c");
        assertThat(options.getPickleOrder()
            .orderPickles(Arrays.asList(a, b, c)), contains(c, a, b));
    }

    private CucumberPickle createPickle(String uri, String name) {
        CucumberFeature feature = TestFeatureParser.parse(uri, "" +
            "Feature: Test feature\n" +
            "  Scenario: " + name + "\n" +
            "     Given I have 4 cukes in my belly\n"
        );
        return feature.getPickles().get(0);
    }

    @Test
    void ensure_invalid_ordertype_is_not_allowed() {
        Executable testMethod = () -> new CommandlineOptionsParser()
            .parse("--order", "invalid")
            .build();
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo("Invalid order. Must be either reverse, random or random:<long>")));
    }

    @Test
    void ensure_less_than_1_count_is_not_allowed() {
        Executable testMethod = () -> new CommandlineOptionsParser()
            .parse("--count", "0")
            .build();
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo("--count must be > 0")));
    }

    @Test
    void loads_no_features_when_rerun_file_specified_in_cucumber_options_property_is_empty() {
        properties.put(OPTIONS_PROPERTY_NAME, "@src/test/resources/io/cucumber/core/options/runtime-options-empty-rerun.txt");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(singletonList("src/test/resources/cucumber/runtime/formatter"))
            .build();
        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(properties)
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
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
