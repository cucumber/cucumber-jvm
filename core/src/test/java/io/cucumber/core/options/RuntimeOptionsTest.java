package io.cucumber.core.options;

import cucumber.api.Plugin;
import cucumber.api.SnippetType;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.StrictAware;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Env;
import cucumber.runtime.Shellwords;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import gherkin.events.PickleEvent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeOptionsTest {

    private final Properties properties = new Properties();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private ResourceLoader resourceLoader;

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

    private static void mockFileResource(ResourceLoader resourceLoader, String path, String feature)
        throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(feature.getBytes(UTF_8)));
        when(resourceLoader.resources(uri(path), null)).thenReturn(singletonList(resource));
    }

    @Test
    public void has_version_from_properties_file() {
        assertTrue(RuntimeOptionsParser.VERSION.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
    }

    @Test
    public void has_usage() {
        RuntimeOptionsParser.loadUsageTextIfNeeded();
        assertThat(RuntimeOptionsParser.usageText, startsWith("Usage"));
    }

    @Test
    public void assigns_feature_paths() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else")));
    }

    @Test
    public void strips_line_filters_from_feature_paths_and_put_them_among_line_filters() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else.feature:3")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), singleton(3)));
    }

    @Test
    public void select_multiple_lines_in_a_features() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else.feature:3:5")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), lines));
    }


    @Test
    public void combines_line_filters_from_repeated_features() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("somewhere_else.feature:3", "somewhere_else.feature:5")
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), lines));
    }

    @Test
    public void assigns_filters_from_tags() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--tags", "@keep_this")
            .build();
        assertThat(options.getTagFilters(), contains("@keep_this"));
    }

    @Test
    public void trims_options() {
        assertThat(Shellwords.parse("  --glue  somewhere   somewhere_else"),
            contains("--glue", "somewhere", "somewhere_else"));
    }

    @Test
    public void assigns_glue() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--glue", "somewhere")
            .build();
        assertThat(options.getGlue(), contains(uri("classpath:somewhere")));
    }

    @Test
    public void creates_html_formatter() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "html:target/some/dir", "--glue", "somewhere")
            .build();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("cucumber.runtime.formatter.HTMLFormatter"));
    }

    @Test
    public void creates_progress_formatter_as_default() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse()
            .addDefaultFormatterIfNotPresent()
            .build();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));
        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("cucumber.runtime.formatter.ProgressFormatter"));
    }

    @Test
    public void creates_progress_formatter_when_no_formatter_plugin_is_specified() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "cucumber.runtime.formatter.AnyStepDefinitionReporter")
            .addDefaultFormatterIfNotPresent()
            .build();

        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.ProgressFormatter")));
    }

    @Test
    public void creates_default_summary_printer_when_no_summary_printer_plugin_is_specified() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "pretty")
            .addDefaultSummaryPrinterIfNotPresent()
            .build();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
    }

    @Test
    public void creates_null_summary_printer() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--plugin", "null_summary", "--glue", "somewhere")
            .build();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.NullSummaryPrinter")));
        assertThat(plugins.getPlugins(), not(hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter"))));
    }

    @Test
    public void assigns_strict() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--strict")
            .build();
        assertTrue(options.isStrict());
    }

    @Test
    public void assigns_strict_short() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("-s")
            .build();
        assertTrue(options.isStrict());
    }

    @Test
    public void default_strict() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse()
            .build();
        assertThat(options.isStrict(), is(false));
    }

    @Test
    public void assigns_wip() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--wip")
            .build();
        assertThat(options.isWip(), is(true));
    }

    @Test
    public void assigns_wip_short() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("-w")
            .build();
        assertThat(options.isWip(), is(true));
    }

    @Test
    public void default_wip() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse()
            .build();
        assertThat(options.isWip(), is(false));
    }

    @Test
    public void name_without_spaces_is_preserved() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--name", "someName")
            .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("someName"));
    }

    @Test
    public void name_with_spaces_is_preserved() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--name", "some Name")
            .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    public void ensure_name_with_spaces_works_with_cucumber_options() {
        properties.setProperty("cucumber.options", "--name 'some Name'");
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build();
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    public void ensure_name_with_spaces_works_with_args() {
        assertThat(Shellwords.parse("--name 'some Name'"), contains("--name", "some Name"));
    }

    @Test
    public void assigns_single_junit_option() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--junit,option")
            .build();
        assertThat(options.getJunitOptions(), contains("option"));
    }

    @Test
    public void assigns_multiple_junit_options() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--junit,option1,option2=value")
            .build();
        assertThat(options.getJunitOptions(), contains("option1", "option2=value"));
    }

    @Test
    public void clobbers_junit_options_from_cli_if_junit_options_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--junit,option_from_property");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--junit,option_to_be_clobbered")
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getJunitOptions(), contains("option_from_property"));
    }

    @Test
    public void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {

        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--glue lookatme this_clobbers_feature_paths");
        Env env = new Env(properties);

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--strict", "--glue", "somewhere", "somewhere_else")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(env)
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:this_clobbers_feature_paths")));
        assertThat(options.getGlue(), contains(uri("classpath:lookatme")));
        assertTrue(options.isStrict());
    }

    @Test
    public void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        properties.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--glue", "somewhere"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getGlue(), contains(uri("classpath:somewhere")));
    }

    @Test
    public void clobbers_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--tags @clobber_with_this");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--tags", "@should_be_clobbered"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getTagFilters(), contains("@clobber_with_this"));
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_line_filters_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "path/file.feature:3");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--tags", "@should_be_clobbered", "--name", "should_be_clobbered")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getTagFilters(), emptyCollectionOf(String.class));
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_rerun_file_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "@src/test/resources/cucumber/runtime/runtime-options-rerun.txt");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--tags", "@should_be_clobbered", "--name", "should_be_clobbered")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getTagFilters(), emptyCollectionOf(String.class));
        assertThat(options.getLineFilters(), hasEntry(uri("file:this/should/be/rerun.feature"), singleton(12)));
    }

    @Test
    public void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--strict");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--tags", "@keep_this"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getTagFilters(), contains("@keep_this"));
    }

    @Test
    public void clobbers_features_from_cli_if_features_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("old", "older"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:new"), uri("file:newer")));
    }

    @Test
    public void strips_lines_from_features_from_cli_if_filters_are_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--tags @Tag");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("path/file.feature:3")
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:path/file.feature")));
    }

    @Test
    public void strips_lines_from_rerun_file_from_cli_if_filters_are_specified_in_cucumber_options_property()
        throws IOException {
        properties.setProperty("cucumber.options", "--tags @Tag");
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/file.feature:3\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);
        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:path/file.feature")));
    }

    @Test
    public void preserves_features_from_cli_if_features_not_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("old", "older"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:old"), uri("file:older")));

    }

    @Test
    public void clobbers_line_filters_from_cli_if_features_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(asList("--tags", "@keep_this", "path/file1.feature:1"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), contains(uri("file:new"), uri("file:newer")));
        assertThat(options.getTagFilters(), contains("@keep_this"));
    }

    @Test
    public void clobbers_formatter_plugins_from_cli_if_formatters_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "html:target/some/dir", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.PrettyFormatter")));
        assertThat(plugins.getPlugins(), not(hasItem(plugin("cucumber.runtime.formatter.HTMLFormatter"))));
    }

    @Test
    public void adds_to_formatter_plugins_with_add_plugin_option() {
        properties.setProperty("cucumber.options", "--add-plugin pretty");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "html:target/some/dir", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.HTMLFormatter")));
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.PrettyFormatter")));
    }

    @Test
    public void clobbers_summary_plugins_from_cli_if_summary_printer_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "null_summary", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
        assertThat(plugins.getPlugins(), not(hasItem(plugin("cucumber.runtime.formatter.NullSummaryPrinter"))));
    }

    @Test
    public void adds_to_summary_plugins_with_add_plugin_option() {
        properties.setProperty("cucumber.options", "--add-plugin default_summary");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "null_summary", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.NullSummaryPrinter")));
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
    }

    @Test
    public void does_not_clobber_plugins_of_different_type_when_specifying_plugins_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--plugin", "pretty", "--glue", "somewhere")
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.PrettyFormatter")));
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
    }

    @Test
    public void allows_removal_of_strict_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--no-strict");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("--strict")
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.isStrict(), is(false));
    }

    @Test
    public void fail_on_unsupported_options() {
        expectedException.expectMessage("Unknown option: -concreteUnsupportedOption");
        new CommandlineOptionsParser()
            .parse(asList("-concreteUnsupportedOption", "somewhere", "somewhere_else"))
            .build();
    }

    @Test
    public void threads_default_1() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse(Collections.<String>emptyList())
            .build();
        assertThat(options.getThreads(), is(1));
    }

    @Test
    public void ensure_threads_param_is_used() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--threads", "10")
            .build();
        assertThat(options.getThreads(), is(10));
    }

    @Test
    public void ensure_less_than_1_thread_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("--threads must be > 0");
        new CommandlineOptionsParser()
            .parse("--threads", "0")
            .build();
    }

    @Test
    public void set_monochrome_on_color_aware_formatters() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--monochrome", "--plugin", AwareFormatter.class.getName())
            .build();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isMonochrome(), is(true));
    }

    @Test
    public void set_strict_on_strict_aware_formatters() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--strict", "--plugin", AwareFormatter.class.getName())
            .build();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), options);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isStrict(), is(true));

    }

    @Test
    public void ensure_default_snippet_type_is_underscore() {
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(Collections.<String>emptyList())
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getSnippetType(), is(SnippetType.UNDERSCORE));
    }

    @Test
    public void set_snippet_type() {
        properties.setProperty("cucumber.options", "--snippets camelcase");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(Collections.<String>emptyList())
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getSnippetType(), is(SnippetType.CAMELCASE));
    }

    @Test
    public void ordertype_default_none() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse(Collections.<String>emptyList())
            .build();
        PickleEvent a = new PickleEvent("a", null);
        PickleEvent b = new PickleEvent("b", null);
        assertThat(options.getPickleOrder()
            .orderPickleEvents(Arrays.asList(a, b)), contains(a, b));
    }

    @Test
    public void ensure_ordertype_reverse_is_used() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--order", "reverse")
            .build();
        PickleEvent a = new PickleEvent("a", null);
        PickleEvent b = new PickleEvent("b", null);
        assertThat(options.getPickleOrder()
            .orderPickleEvents(Arrays.asList(a, b)), contains(b, a));
    }

    @Test
    public void ensure_ordertype_random_is_used() {
        new CommandlineOptionsParser()
            .parse("--order", "random")
            .build();
    }

    @Test
    public void ensure_ordertype_random_with_seed_is_used() {
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("--order", "random:5000")
            .build();
        PickleEvent a = new PickleEvent("a", null);
        PickleEvent b = new PickleEvent("b", null);
        PickleEvent c = new PickleEvent("c", null);
        assertThat(options.getPickleOrder()
            .orderPickleEvents(Arrays.asList(a, b, c)), contains(c, a, b));
    }

    @Test
    public void ensure_invalid_ordertype_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("Invalid order. Must be either reverse, random or random:<long>");
        new CommandlineOptionsParser()
            .parse("--order", "invalid")
            .build();
    }

    @Test
    public void ensure_less_than_1_count_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("--count must be > 0");
        new CommandlineOptionsParser()
            .parse("--count", "0")
            .build();
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_carriage_return() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "\r";
        mockFileResource(resourceLoader, rerunPath, rerunFile);
        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();
        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void loads_features_specified_in_rerun_file() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/bar.feature:2\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), contains(uri("file:path/bar.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:path/bar.feature"), singleton(2)));
    }

    @Test
    public void loads_features_specified_in_rerun_file_with_empty_cucumber_options() throws Exception {
        properties.put("cucumber.options", "");
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/bar.feature:2\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);
        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), contains(uri("file:path/bar.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:path/bar.feature"), singleton(2)));
    }

    @Test
    public void clobbers_features_from_rerun_file_specified_in_cli_if_features_specified_in_cucumber_options_property() throws Exception {
        properties.put("cucumber.options", "file:path/foo.feature");
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/bar.feature:2\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        RuntimeOptions options = new EnvironmentOptionsParser(resourceLoader)
            .parse(new Env(properties))
            .build(runtimeOptions);

        assertThat(options.getFeaturePaths(), contains(uri("file:path/foo.feature")));
        assertThat(options.getLineFilters().size(), is(0));
    }

    @Test
    public void loads_no_features_when_rerun_file_is_empty() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }


    @Test
    public void loads_no_features_when_rerun_file_specified_in_cucumber_options_property_is_empty() throws Exception {
        properties.setProperty("cucumber.options", "@src/test/resources/cucumber/runtime/runtime-options-empty-rerun.txt");
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(singletonList("src/test/resources/cucumber/runtime/formatter"))
            .build();
        RuntimeOptions options = new EnvironmentOptionsParser()
            .parse(new Env(properties))
            .build(runtimeOptions);
        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" +  rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line_and_carriage_return() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "\r\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void last_new_line_is_optinal() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/bar.feature" + ":2";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:path/bar.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:path/bar.feature"), singleton(2)));
    }

    @Test
    public void loads_features_specified_in_rerun_file_from_classpath_when_not_in_file_system() throws Exception {
        String featurePath = "classpath:path/bar.feature";
        URI featureUri = uri(featurePath);
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = featurePath + ":2";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), contains(featureUri));
        assertThat(options.getLineFilters(), hasEntry(featureUri, singleton(2)));
    }

    @Test
    public void understands_whitespace_in_rerun_filepath() throws Exception {
        String featurePath1 = "My Documents/tests/bar.feature";
        String rerunPath = "file:rerun.txt";
        String rerunFile = featurePath1 + ":2\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();
        assertThat(options.getFeaturePaths(), contains(uri("file:My%20Documents/tests/bar.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:My%20Documents/tests/bar.feature"), singleton(2)));
    }

    @Test
    public void understands_rerun_files_separated_by_with_whitespace() throws Exception {
        String featurePath1 = "file:/home/users/mp/My%20Documents/tests/bar.feature";
        String featurePath2 = "file:/home/users/mp/My%20Documents/tests/foo.feature";
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = featurePath1 + ":2 " + featurePath2 + ":4";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();

        assertThat(options.getFeaturePaths(), is(asList(uri(featurePath1), uri(featurePath2))));

    }

    @Test
    public void understands_rerun_files_without_separation_in_rerun_filepath() throws Exception {
        String featurePath1 = "file:/home/users/mp/My%20Documents/tests/bar.feature";
        String featurePath2 = "file:/home/users/mp/My%20Documents/tests/foo.feature";
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = featurePath1 + ":2" + featurePath2 + ":4";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new CommandlineOptionsParser(resourceLoader)
            .parse("@" + rerunPath)
            .build();
        assertThat(options.getFeaturePaths(), contains(uri(featurePath1), uri(featurePath2)));
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
