package cucumber.runtime;

import cucumber.runner.EventBus;
import cucumber.api.SnippetType;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.StrictAware;
import cucumber.runtime.formatter.FormatterSpy;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class RuntimeOptionsTest {
    @Test
    public void has_version_from_properties_file() {
        assertTrue(RuntimeOptions.VERSION.startsWith("2.0"));
    }

    @Test
    public void has_usage() {
        RuntimeOptions.loadUsageTextIfNeeded();
        assertTrue(RuntimeOptions.usageText.startsWith("Usage"));
    }

    @Test
    public void assigns_feature_paths() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
    }

    @Test
    public void strips_line_filters_from_feature_paths_and_put_them_among_line_filters() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere somewhere_else:3");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
        Map<String, List<Long>> expectedLineFilters = new HashMap<String, List<Long>>(Collections.singletonMap("somewhere_else", asList(3L)));
        assertEquals(expectedLineFilters, options.getLineFilters(mock(ResourceLoader.class)));
    }

    @Test
    public void assigns_filters_from_tags() {
        RuntimeOptions options = new RuntimeOptions("--tags @keep_this somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
        assertEquals(Arrays.<String>asList("@keep_this"), options.getTagFilters());
    }

    @Test
    public void strips_options() {
        RuntimeOptions options = new RuntimeOptions("  --glue  somewhere   somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
    }

    @Test
    public void assigns_glue() {
        RuntimeOptions options = new RuntimeOptions("--glue somewhere");
        assertEquals(asList("somewhere"), options.getGlue());
    }

    @Test @org.junit.Ignore
    public void creates_html_formatter() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "html:some/dir", "--glue", "somewhere"));
        assertEquals("cucumber.runtime.formatter.HTMLFormatter", options.getPlugins().get(0).getClass().getName());
    }

    @Test
    public void creates_progress_formatter_as_default() {
        RuntimeOptions options = new RuntimeOptions(asList("--glue", "somewhere"));
        assertEquals("cucumber.runtime.formatter.ProgressFormatter", options.getPlugins().get(0).getClass().getName());
    }

    @Test
    public void creates_progress_formatter_when_no_formatter_plugin_is_specified() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "cucumber.runtime.formatter.AnyStepDefinitionReporter", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.formatter.ProgressFormatter");
    }

    @Test
    public void creates_default_summary_printer_when_no_summary_printer_plugin_is_specified() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "pretty", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.DefaultSummaryPrinter");
    }

    @Test
    public void creates_null_summary_printer() {
        RuntimeOptions options = new RuntimeOptions(asList("--plugin", "null_summary", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.NullSummaryPrinter");
        assertPluginNotExists(options.getPlugins(), "cucumber.runtime.DefaultSummaryPrinter");
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
        assertFalse(options.isStrict());
    }

    @Test
    public void name_without_spaces_is_preserved() {
        RuntimeOptions options = new RuntimeOptions(asList("--name", "someName"));
        Pattern actualPattern = (Pattern) options.getNameFilters().iterator().next();
        assertEquals("someName", actualPattern.pattern());
    }

    @Test
    public void name_with_spaces_is_preserved() {
        RuntimeOptions options = new RuntimeOptions(asList("--name", "some Name"));
        Pattern actualPattern = (Pattern) options.getNameFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void ensure_name_with_spaces_works_with_cucumber_options() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--name 'some Name'");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), Collections.<String>emptyList());
        Pattern actualPattern = (Pattern) options.getNameFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void ensure_name_with_spaces_works_with_args() {
        RuntimeOptions options = new RuntimeOptions("--name 'some Name'");
        Pattern actualPattern = (Pattern) options.getNameFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void assigns_single_junit_option() {
        RuntimeOptions options = new RuntimeOptions(asList("--junit,option"));
        assertEquals(asList("option"), options.getJunitOptions());
    }

    @Test
    public void assigns_multiple_junit_options() {
        RuntimeOptions options = new RuntimeOptions(asList("--junit,option1,option2=value"));
        assertEquals(asList("option1", "option2=value"), options.getJunitOptions());
    }

    @Test
    public void clobbers_junit_options_from_cli_if_junit_options_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--junit,option_from_property");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--junit,option_to_be_clobbered"));
        assertEquals(asList("option_from_property"), runtimeOptions.getJunitOptions());
    }

    @Test
    public void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--glue lookatme this_clobbers_feature_paths");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--strict", "--glue", "somewhere", "somewhere_else"));
        assertEquals(asList("this_clobbers_feature_paths"), options.getFeaturePaths());
        assertEquals(asList("lookatme"), options.getGlue());
        assertTrue(options.isStrict());
    }

    @Test
    public void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--glue", "somewhere"));
        assertEquals(asList("somewhere"), runtimeOptions.getGlue());
    }

    @Test
    public void clobbers_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @clobber_with_this");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--tags", "@should_be_clobbered"));
        assertEquals(asList("@clobber_with_this"), runtimeOptions.getTagFilters());
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_line_filters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "path/file.feature:3");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--tags", "@should_be_clobbered", "--name", "should_be_clobbered"));
        assertEquals(Collections.<Object>emptyList(), runtimeOptions.getTagFilters());
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_rerun_file_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "@rerun.txt");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--tags", "@should_be_clobbered", "--name", "should_be_clobbered"));
        assertEquals(Collections.<Object>emptyList(), runtimeOptions.getTagFilters());
    }

    @Test
    public void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--strict");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--tags", "@keep_this"));
        assertEquals(asList("@keep_this"), runtimeOptions.getTagFilters());
    }

    @Test
    public void clobbers_features_from_cli_if_features_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("old", "older"));
        assertEquals(asList("new", "newer"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void strips_lines_from_features_from_cli_if_filters_are_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @Tag");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("path/file.feature:3"));
        assertEquals(asList("path/file.feature"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void preserves_features_from_cli_if_features_not_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("old", "older"));
        assertEquals(asList("old", "older"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void clobbers_line_filters_from_cli_if_features_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--tags", "@keep_this", "path/file1.feature:1"));
        assertEquals(asList("new", "newer"), runtimeOptions.getFeaturePaths());
        assertEquals(asList("@keep_this"), runtimeOptions.getTagFilters());
    }

    @Test @org.junit.Ignore
    public void clobbers_formatter_plugins_from_cli_if_formatters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "html:some/dir", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.formatter.CucumberPrettyFormatter");
        assertPluginNotExists(options.getPlugins(), "cucumber.runtime.formatter.HTMLFormatter");
    }

    @Test @org.junit.Ignore
    public void adds_to_formatter_plugins_with_add_plugin_option() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--add-plugin pretty");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "html:some/dir", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.formatter.HTMLFormatter");
        assertPluginExists(options.getPlugins(), "cucumber.runtime.formatter.CucumberPrettyFormatter");
    }

    @Test
    public void clobbers_summary_plugins_from_cli_if_summary_printer_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "null_summary", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.DefaultSummaryPrinter");
        assertPluginNotExists(options.getPlugins(), "cucumber.runtime.NullSummaryPrinter");
    }

    @Test
    public void adds_to_summary_plugins_with_add_plugin_option() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--add-plugin default_summary");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "null_summary", "--glue", "somewhere"));
        assertPluginExists(options.getPlugins(), "cucumber.runtime.NullSummaryPrinter");
        assertPluginExists(options.getPlugins(), "cucumber.runtime.DefaultSummaryPrinter");
    }

    @Test
    public void does_not_clobber_plugins_of_different_type_when_specifying_plugins_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions options = new RuntimeOptions(new Env(properties), asList("--plugin", "pretty", "--glue", "somewhere"));
//        assertPluginExists(options.getPlugins(), "cucumber.runtime.formatter.CucumberPrettyFormatter");
        assertPluginExists(options.getPlugins(), "cucumber.runtime.DefaultSummaryPrinter");
    }

    @Test
    public void allows_removal_of_strict_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--no-strict");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), asList("--strict"));
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void fail_on_unsupported_options() {
        try {
            new RuntimeOptions(asList("-concreteUnsupportedOption", "somewhere", "somewhere_else"));
            fail();
        } catch (CucumberException e) {
            assertEquals("Unknown option: -concreteUnsupportedOption", e.getMessage());
        }
    }

    @Test
    public void set_monochrome_on_color_aware_formatters() throws Exception {
        PluginFactory factory = mock(PluginFactory.class);
        Formatter colorAwareFormatter = mock(Formatter.class, withSettings().extraInterfaces(ColorAware.class));
        when(factory.create("progress")).thenReturn(colorAwareFormatter);

        RuntimeOptions options = new RuntimeOptions(new Env(), factory, asList("--monochrome", "--plugin", "progress"));
        options.getPlugins();

        verify((ColorAware) colorAwareFormatter).setMonochrome(true);
    }

    @Test
    public void set_strict_on_strict_aware_formatters() throws Exception {
        PluginFactory factory = mock(PluginFactory.class);
        Formatter strictAwareFormatter = mock(Formatter.class, withSettings().extraInterfaces(StrictAware.class));
        when(factory.create("junit:out/dir")).thenReturn(strictAwareFormatter);

        RuntimeOptions options = new RuntimeOptions(new Env(), factory, asList("--strict", "--plugin", "junit:out/dir"));
        options.getPlugins();

        verify((StrictAware) strictAwareFormatter).setStrict(true);
    }

    @Test
    public void ensure_default_snippet_type_is_underscore() {
        Properties properties = new Properties();
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), Collections.<String>emptyList());
        assertEquals(SnippetType.UNDERSCORE, runtimeOptions.getSnippetType());
    }

    @Test
    public void set_snippet_type() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--snippets camelcase");
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Env(properties), Collections.<String>emptyList());
        assertEquals(SnippetType.CAMELCASE, runtimeOptions.getSnippetType());
    }

    private void assertPluginExists(List<Object> plugins, String pluginName) {
        assertTrue(pluginName + " not found among the plugins", pluginExists(plugins, pluginName));
    }

    private void assertPluginNotExists(List<Object> plugins, String pluginName) {
        assertFalse(pluginName + " found among the plugins", pluginExists(plugins, pluginName));
    }

    private boolean pluginExists(List<Object> plugins, String pluginName) {
        boolean found = false;
        for (Object plugin : plugins) {
            if (plugin.getClass().getName() == pluginName) {
                found = true;
            }
        }
        return found;
    }
}
