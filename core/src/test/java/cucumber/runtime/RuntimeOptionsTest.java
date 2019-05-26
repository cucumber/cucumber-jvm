package cucumber.runtime;

import cucumber.api.Plugin;
import cucumber.api.SnippetType;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.StrictAware;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.order.NoneOrderType;
import cucumber.runtime.order.RandomOrderType;
import cucumber.runtime.order.ReverseOrderType;

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
import static org.hamcrest.CoreMatchers.instanceOf;
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
        assertTrue(RuntimeOptions.VERSION.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
    }

    @Test
    public void has_usage() {
        RuntimeOptions.loadUsageTextIfNeeded();
        assertThat(RuntimeOptions.usageText, startsWith("Usage"));
    }

    @Test
    public void assigns_feature_paths() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere somewhere_else");
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else")));
    }

    @Test
    public void strips_line_filters_from_feature_paths_and_put_them_among_line_filters() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere somewhere_else.feature:3");
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), singleton(3)));
    }


    @Test
    public void select_multiple_lines_in_a_features() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere somewhere_else.feature:3:5");
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), lines));
    }


    @Test
    public void combines_line_filters_from_repeated_features() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere somewhere_else.feature:3 somewhere_else.feature:5");
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else.feature")));
        Set<Integer> lines = new HashSet<>(asList(3, 5));
        assertThat(options.getLineFilters(), hasEntry(uri("file:somewhere_else.feature"), lines));
    }

    @Test
    public void assigns_filters_from_tags() {
        RuntimeOptions options = new RuntimeOptions("--tags @keep_this somewhere_else");
        assertThat(options.getFeaturePaths(), contains(uri("file:somewhere_else")));
        assertThat(options.getTagFilters(), contains("@keep_this"));
    }

    @Test
    public void strips_options() {
        RuntimeOptions options = new RuntimeOptions("  --glue  somewhere   somewhere_else");
        assertThat(options.getFeaturePaths(), is(singletonList(uri("file:somewhere_else"))));
    }

    @Test
    public void assigns_glue() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere");
        assertThat(options.getGlue(), contains(uri("classpath:somewhere")));
    }

    @Test
    public void creates_html_formatter() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "html:some/dir", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("cucumber.runtime.formatter.HTMLFormatter"));
    }

    @Test
    public void creates_progress_formatter_as_default() {
        RuntimeOptions options = new RuntimeOptions(asList("--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins().get(0).getClass().getName(), is("cucumber.runtime.formatter.ProgressFormatter"));
    }

    @Test
    public void creates_progress_formatter_when_no_formatter_plugin_is_specified() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "cucumber.runtime.formatter.AnyStepDefinitionReporter", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.ProgressFormatter")));
    }

    @Test
    public void creates_default_summary_printer_when_no_summary_printer_plugin_is_specified() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "pretty", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
    }

    @Test
    public void creates_null_summary_printer() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "null_summary", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.NullSummaryPrinter")));
        assertThat(plugins.getPlugins(), not(hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter"))));
    }

    @Test
    public void assigns_strict() {
        RuntimeOptions options = new RuntimeOptions(asList("--strict", "--glue", "somewhere"));
        assertTrue(options.isStrict());
    }

    @Test
    public void assigns_strict_short() {
        RuntimeOptions options = new RuntimeOptions(asList("-s", "--glue", "somewhere"));
        assertTrue(options.isStrict());
    }

    @Test
    public void default_strict() {
        RuntimeOptions options = new RuntimeOptions(asList("--glue", "somewhere"));
        assertThat(options.isStrict(), is(false));
    }

    @Test
    public void assigns_wip() {
        RuntimeOptions options = new RuntimeOptions(asList("--wip", "--glue", "somewhere"));
        assertThat(options.isWip(), is(true));
    }

    @Test
    public void assigns_wip_short() {
        RuntimeOptions options = new RuntimeOptions(asList("-w", "--glue", "somewhere"));
        assertThat(options.isWip(), is(true));
    }

    @Test
    public void default_wip() {
        RuntimeOptions options = new RuntimeOptions(asList("--glue", "somewhere"));
        assertThat(options.isWip(), is(false));
    }

    @Test
    public void name_without_spaces_is_preserved() {
        RuntimeOptions options = new RuntimeOptions(asList("--name", "someName"));
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("someName"));
    }

    @Test
    public void name_with_spaces_is_preserved() {
        RuntimeOptions options = new RuntimeOptions(asList("--name", "some Name"));
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    public void ensure_name_with_spaces_works_with_cucumber_options() {
        properties.setProperty("cucumber.options", "--name 'some Name'");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), Collections.<String>emptyList());
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    public void ensure_name_with_spaces_works_with_args() {
        RuntimeOptions options = new RuntimeOptions("--name 'some Name'");
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertThat(actualPattern.pattern(), is("some Name"));
    }

    @Test
    public void assigns_single_junit_option() {
        RuntimeOptions options = new RuntimeOptions("--junit,option");
        assertThat(options.getJunitOptions(), contains("option"));
    }

    @Test
    public void assigns_multiple_junit_options() {
        RuntimeOptions options = new RuntimeOptions("--junit,option1,option2=value");
        assertThat(options.getJunitOptions(), contains("option1", "option2=value"));
    }

    @Test
    public void clobbers_junit_options_from_cli_if_junit_options_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--junit,option_from_property");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--junit,option_to_be_clobbered"));
        assertThat(options.getJunitOptions(), contains("option_from_property"));
    }

    @Test
    public void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {
        properties.setProperty("cucumber.options", "--glue lookatme this_clobbers_feature_paths");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--strict", "--glue", "somewhere", "somewhere_else"));
        assertThat(options.getFeaturePaths(), contains(uri("file:this_clobbers_feature_paths")));
        assertThat(options.getGlue(), contains(uri("classpath:lookatme")));
        assertTrue(options.isStrict());
    }

    @Test
    public void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        properties.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--glue", "somewhere"));
        assertThat(options.getGlue(), contains(uri("classpath:somewhere")));
    }

    @Test
    public void clobbers_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--tags @clobber_with_this");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--tags", "@should_be_clobbered"));
        assertThat(options.getTagFilters(), contains("@clobber_with_this"));
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_line_filters_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "path/file.feature:3");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--tags", "@should_be_clobbered", "--name", "should_be_clobbered"));
        assertThat(options.getTagFilters(), emptyCollectionOf(String.class));
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_rerun_file_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "@src/test/resources/cucumber/runtime/runtime-options-rerun.txt");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--tags", "@should_be_clobbered", "--name", "should_be_clobbered"));
        assertThat(options.getTagFilters(), emptyCollectionOf(String.class));
        assertThat(options.getLineFilters(), hasEntry(uri("file:this/should/be/rerun.feature"), singleton(12)));
    }

    @Test
    public void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--strict");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--tags", "@keep_this"));
        assertThat(options.getTagFilters(), contains("@keep_this"));
    }

    @Test
    public void clobbers_features_from_cli_if_features_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("old", "older"));
        assertThat(options.getFeaturePaths(), contains(uri("file:new"), uri("file:newer")));
    }

    @Test
    public void strips_lines_from_features_from_cli_if_filters_are_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--tags @Tag");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("path/file.feature:3"));
        assertThat(options.getFeaturePaths(), contains(uri("file:path/file.feature")));
    }

    @Test
    public void preserves_features_from_cli_if_features_not_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("old", "older"));
        assertThat(options.getFeaturePaths(), contains(uri("file:old"), uri("file:older")));

    }

    @Test
    public void clobbers_line_filters_from_cli_if_features_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--tags", "@keep_this", "path/file1.feature:1"));
        assertThat(options.getFeaturePaths(), contains(uri("file:new"), uri("file:newer")));
        assertThat(options.getTagFilters(), contains("@keep_this"));
    }

    @Test
    public void clobbers_formatter_plugins_from_cli_if_formatters_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "html:some/dir", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.PrettyFormatter")));
        assertThat(plugins.getPlugins(), not(hasItem(plugin("cucumber.runtime.formatter.HTMLFormatter"))));
    }

    @Test
    public void adds_to_formatter_plugins_with_add_plugin_option() {
        properties.setProperty("cucumber.options", "--add-plugin pretty");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "html:some/dir", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.HTMLFormatter")));
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.PrettyFormatter")));
    }

    @Test
    public void clobbers_summary_plugins_from_cli_if_summary_printer_specified_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "null_summary", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
        assertThat(plugins.getPlugins(), not(hasItem(plugin("cucumber.runtime.formatter.NullSummaryPrinter"))));
    }

    @Test
    public void adds_to_summary_plugins_with_add_plugin_option() {
        properties.setProperty("cucumber.options", "--add-plugin default_summary");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "null_summary", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.NullSummaryPrinter")));
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
    }

    @Test
    public void does_not_clobber_plugins_of_different_type_when_specifying_plugins_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "pretty", "--glue", "somewhere"));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.PrettyFormatter")));
        assertThat(plugins.getPlugins(), hasItem(plugin("cucumber.runtime.formatter.DefaultSummaryPrinter")));
    }

    @Test
    public void allows_removal_of_strict_in_cucumber_options_property() {
        properties.setProperty("cucumber.options", "--no-strict");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--strict"));
        assertThat(options.isStrict(), is(false));
    }

    @Test
    public void fail_on_unsupported_options() {
        expectedException.expectMessage("Unknown option: -concreteUnsupportedOption");
        new RuntimeOptions(asList("-concreteUnsupportedOption", "somewhere", "somewhere_else"));
    }

    @Test
    public void threads_default_1() {
        RuntimeOptions options = new RuntimeOptions(Collections.<String>emptyList());
        assertThat(options.getThreads(), is(1));
    }

    @Test
    public void ensure_threads_param_is_used() {
        RuntimeOptions options = new RuntimeOptions(asList("--threads", "10"));
        assertThat(options.getThreads(), is(10));
    }

    @Test
    public void ensure_less_than_1_thread_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("--threads must be > 0");
        new RuntimeOptions(asList("--threads", "0"));
    }

    @Test
    public void set_monochrome_on_color_aware_formatters() {
        RuntimeOptions options = new RuntimeOptions(new Env(), asList("--monochrome", "--plugin", AwareFormatter.class.getName()));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isMonochrome(), is(true));
    }

    @Test
    public void set_strict_on_strict_aware_formatters() {
        RuntimeOptions options = new RuntimeOptions(new Env(), asList("--strict", "--plugin", AwareFormatter.class.getName()));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), new TimeServiceEventBus(TimeService.SYSTEM), options);
        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertThat(formatter.isStrict(), is(true));

    }

    @Test
    public void ensure_default_snippet_type_is_underscore() {
        RuntimeOptions options = new RuntimeOptions(new Env(properties), Collections.<String>emptyList());
        assertThat(options.getSnippetType(), is(SnippetType.UNDERSCORE));
    }

    @Test
    public void set_snippet_type() {
        properties.setProperty("cucumber.options", "--snippets camelcase");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), Collections.<String>emptyList());
        assertThat(options.getSnippetType(), is(SnippetType.CAMELCASE));
    }
    
    @Test
    public void ordertype_default_none() {
    	RuntimeOptions options = new RuntimeOptions(Collections.<String>emptyList());
        assertThat(options.getOrderType(), instanceOf(NoneOrderType.class));
    }
    
    @Test
    public void ensure_ordertype_none_is_used() {
    	RuntimeOptions options = new RuntimeOptions(asList("--order", "none"));
        assertThat(options.getOrderType(), instanceOf(NoneOrderType.class));
    }
    
    @Test
    public void ensure_ordertype_reverse_is_used() {
    	RuntimeOptions options = new RuntimeOptions(asList("--order", "reverse"));
        assertThat(options.getOrderType(), instanceOf(ReverseOrderType.class));
    }
    
    @Test
    public void ensure_ordertype_random_is_used() {
    	RuntimeOptions options = new RuntimeOptions(asList("--order", "random"));
        assertThat(options.getOrderType(), instanceOf(RandomOrderType.class));
    }
    
    @Test
    public void ensure_ordertype_random_with_valid_count_is_used() {
    	RuntimeOptions options = new RuntimeOptions(asList("--order", "random", "--count", "5"));
        assertThat(options.getOrderType(), instanceOf(RandomOrderType.class));
    }
    
    @Test
    public void ensure_less_than_1_random_ordertype_count_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("--count must be > 0");
        new RuntimeOptions(asList("--order", "random", "--count", "0"));
    }
    
    @Test
    public void loads_no_features_when_rerun_file_contains_carriage_return() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "\r";
        mockFileResource(resourceLoader, rerunPath, rerunFile);
        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));
        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void loads_features_specified_in_rerun_file() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/bar.feature:2\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));

        assertThat(options.getFeaturePaths(), contains(uri("file:path/bar.feature")));
        assertThat(options.getLineFilters(), hasEntry(uri("file:path/bar.feature"), singleton(2)));
    }

    @Test
    public void loads_no_features_when_rerun_file_is_empty() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));

        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));

        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line_and_carriage_return() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "\r\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));

        assertThat(options.getFeaturePaths(), emptyCollectionOf(URI.class));
    }

    @Test
    public void last_new_line_is_optinal() throws Exception {
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = "file:path/bar.feature" + ":2";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));
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

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));

        assertThat(options.getFeaturePaths(), contains(featureUri));
        assertThat(options.getLineFilters(), hasEntry(featureUri, singleton(2)));
    }

    @Test
    public void understands_whitespace_in_rerun_filepath() throws Exception {
        String featurePath1 = "My Documents/tests/bar.feature";
        String rerunPath = "file:rerun.txt";
        String rerunFile = featurePath1 + ":2\n";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));
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

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));

        assertThat(options.getFeaturePaths(), is(asList(uri(featurePath1), uri(featurePath2))));

    }

    @Test
    public void understands_rerun_files_without_separation_in_rerun_filepath() throws Exception {
        String featurePath1 = "file:/home/users/mp/My%20Documents/tests/bar.feature";
        String featurePath2 = "file:/home/users/mp/My%20Documents/tests/foo.feature";
        String rerunPath = "file:path/rerun.txt";
        String rerunFile = featurePath1 + ":2" + featurePath2 + ":4";
        mockFileResource(resourceLoader, rerunPath, rerunFile);

        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), singletonList("@" + rerunPath));
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
