package io.cucumber.core.options;

import io.cucumber.core.api.plugin.Plugin;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.plugin.ColorAware;
import io.cucumber.core.api.plugin.StrictAware;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.TimeService;
import io.cucumber.core.runner.TimeServiceEventBus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RuntimeOptionsTest {

    public final Env env = new Env();
    public final EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
    public final ResourceLoader resourceLoader = new MultiLoader(RuntimeOptionsTest.class.getClassLoader());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void has_version_from_properties_file() {
        assertTrue(RuntimeOptions.VERSION.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
    }

    @Test
    public void has_usage() {
        RuntimeOptions.loadUsageTextIfNeeded();
        assertTrue(RuntimeOptions.usageText.startsWith("Usage"));
    }

    @Test
    public void assigns_feature_paths() {
        RuntimeOptions options = createRuntimeOptions("--glue", "somewhere", "somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
    }

    @Test
    public void strips_line_filters_from_feature_paths_and_put_them_among_line_filters() {
        RuntimeOptions options = createRuntimeOptions("--glue", "somewhere", "somewhere_else:3");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
        assertEquals(singletonMap("somewhere_else", asList(3L)), options.getLineFilters());
    }

    @Test
    public void assigns_filters_from_tags() {
        RuntimeOptions options = createRuntimeOptions("--tags", "@keep_this", "somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
        assertEquals(asList("@keep_this"), options.getTagExpressions());
    }

    @Test
    public void strips_options() {
        RuntimeOptions options = createRuntimeOptions("  --glue", "somewhere", "somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
    }

    @Test
    public void assigns_glue() {
        RuntimeOptions options = createRuntimeOptions("--glue", "somewhere");
        assertEquals(asList("somewhere"), options.getGlue());
    }

    @Test
    public void creates_html_formatter() {
        RuntimeOptions options = createRuntimeOptions("--plugin", "html:target/cucumber-reports", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertEquals("io.cucumber.core.plugin.HTMLFormatter", plugins.getPlugins().get(0).getClass().getName());
    }

    @Test
    public void creates_progress_formatter_as_default() {
        RuntimeOptions options = createRuntimeOptions("--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertEquals("io.cucumber.core.plugin.ProgressFormatter", plugins.getPlugins().get(0).getClass().getName());
    }

    @Test
    public void creates_progress_formatter_when_no_formatter_plugin_is_specified() {
        RuntimeOptions options = createRuntimeOptions("--plugin", "io.cucumber.core.plugin.AnyStepDefinitionReporter", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.ProgressFormatter");
    }

    @Test
    public void creates_default_summary_printer_when_no_summary_printer_plugin_is_specified() {
        RuntimeOptions options = createRuntimeOptions("--plugin", "pretty", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    @Test
    public void creates_null_summary_printer() {
        RuntimeOptions options = createRuntimeOptions("--plugin", "null_summary", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.NullSummaryPrinter");
        assertPluginNotExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    @Test
    public void assigns_strict() {
        RuntimeOptions options = createRuntimeOptions("--strict", "--glue", "somewhere");
        assertTrue(options.isStrict());
    }

    @Test
    public void assigns_strict_short() {
        RuntimeOptions options = createRuntimeOptions("-s", "--glue", "somewhere");
        assertTrue(options.isStrict());
    }

    @Test
    public void default_strict() {
        RuntimeOptions options = createRuntimeOptions("--glue", "somewhere");
        assertFalse(options.isStrict());
    }

    @Test
    public void assigns_wip() {
        RuntimeOptions options = createRuntimeOptions("--wip", "--glue", "somewhere");
        assertTrue(options.isWip());
    }

    @Test
    public void assigns_wip_short() {
        RuntimeOptions options = createRuntimeOptions("-w", "--glue", "somewhere");
        assertTrue(options.isWip());
    }

    @Test
    public void default_wip() {
        RuntimeOptions options = createRuntimeOptions("--glue", "somewhere");
        assertFalse(options.isWip());
    }

    @Test
    public void name_without_spaces_is_preserved() {
        RuntimeOptions options = createRuntimeOptions("--name", "someName");
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertEquals("someName", actualPattern.pattern());
    }

    @Test
    public void name_with_spaces_is_preserved() {
        RuntimeOptions options = createRuntimeOptions("--name", "some Name");
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void ensure_name_with_spaces_works_with_cucumber_options() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--name 'some Name'");
        RuntimeOptions options = new RuntimeOptions(resourceLoader, new Env(properties), Collections.emptyList());
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void ensure_name_with_spaces_works_with_args() {
        RuntimeOptions options = createRuntimeOptions("--name", "some Name");
        Pattern actualPattern = options.getNameFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void assigns_single_junit_option() {
        RuntimeOptions options = createRuntimeOptions("--junit,option");
        assertEquals(asList("option"), options.getJunitOptions());
    }

    @Test
    public void assigns_multiple_junit_options() {
        RuntimeOptions options = createRuntimeOptions("--junit,option1,option2=value");
        assertEquals(asList("option1", "option2=value"), options.getJunitOptions());
    }

    @Test
    public void clobbers_junit_options_from_cli_if_junit_options_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--junit,option_from_property");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--junit,option_to_be_clobbered");
        assertEquals(asList("option_from_property"), runtimeOptions.getJunitOptions());
    }

    @Test
    public void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--glue lookatme this_clobbers_feature_paths");
        RuntimeOptions options = createRuntimeOptions(properties, "--strict", "--glue", "somewhere", "somewhere_else");
        assertEquals(asList("this_clobbers_feature_paths"), options.getFeaturePaths());
        assertEquals(asList("lookatme"), options.getGlue());
        assertTrue(options.isStrict());
    }

    @Test
    public void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--glue", "somewhere");
        assertEquals(asList("somewhere"), runtimeOptions.getGlue());
    }

    @Test
    public void clobbers_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @clobber_with_this");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--tags", "@should_be_clobbered");
        assertEquals(asList("@clobber_with_this"), runtimeOptions.getTagExpressions());
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_line_filters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "path/file.feature:3");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--tags", "@should_be_clobbered", "--name", "should_be_clobbered");
        assertEquals(Collections.emptyList(), runtimeOptions.getTagExpressions());
    }


    @Test
    public void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--strict");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--tags", "@keep_this");
        assertEquals(asList("@keep_this"), runtimeOptions.getTagExpressions());
    }

    @Test
    public void clobbers_features_from_cli_if_features_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "old", "older");
        assertEquals(asList("new", "newer"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void strips_lines_from_features_from_cli_if_filters_are_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @Tag");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "path/file.feature:3");
        assertEquals(asList("path/file.feature"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void preserves_features_from_cli_if_features_not_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "old", "older");
        assertEquals(asList("old", "older"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void clobbers_line_filters_from_cli_if_features_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "new newer");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--tags", "@keep_this", "path/file1.feature:1");
        assertEquals(asList("new", "newer"), runtimeOptions.getFeaturePaths());
        assertEquals(asList("@keep_this"), runtimeOptions.getTagExpressions());
    }

    @Test
    public void clobbers_formatter_plugins_from_cli_if_formatters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin pretty");
        RuntimeOptions options = createRuntimeOptions(properties, "--plugin", "html:target/cucumber-reports", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.PrettyFormatter");
        assertPluginNotExists(plugins.getPlugins(), "io.cucumber.core.plugin.HTMLFormatter");
    }

    @Test
    public void adds_to_formatter_plugins_with_add_plugin_option() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--add-plugin pretty");
        RuntimeOptions options = createRuntimeOptions(properties, "--plugin", "html:target/cucumber-reports", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.HTMLFormatter");
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.PrettyFormatter");
    }

    @Test
    public void clobbers_summary_plugins_from_cli_if_summary_printer_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions options = createRuntimeOptions(properties, "--plugin", "null_summary", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
        assertPluginNotExists(plugins.getPlugins(), "io.cucumber.core.plugin.NullSummaryPrinter");
    }

    @Test
    public void adds_to_summary_plugins_with_add_plugin_option() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--add-plugin default_summary");
        RuntimeOptions options = createRuntimeOptions(properties, "--plugin", "null_summary", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.NullSummaryPrinter");
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    @Test
    public void does_not_clobber_plugins_of_different_type_when_specifying_plugins_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--plugin default_summary");
        RuntimeOptions options = createRuntimeOptions(properties, "--plugin", "pretty", "--glue", "somewhere");
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
//        assertPluginExists(options.getPlugins(), "io.cucumber.core.plugin.CucumberPrettyFormatter");
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    @Test
    public void allows_removal_of_strict_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--no-strict");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties, "--strict");
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void fail_on_unsupported_options() {
        try {
            createRuntimeOptions("-concreteUnsupportedOption", "somewhere", "somewhere_else");
            fail();
        } catch (CucumberException e) {
            assertEquals("Unknown option: -concreteUnsupportedOption", e.getMessage());
        }
    }

    @Test
    public void threads_default_1() {
        RuntimeOptions options = createRuntimeOptions();
        assertEquals(1, options.getThreads());
    }

    @Test
    public void ensure_threads_param_is_used() {
        RuntimeOptions options = createRuntimeOptions("--threads", "10");
        assertEquals(10, options.getThreads());
    }

    @Test
    public void ensure_less_than_1_thread_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("--threads must be > 0");
        createRuntimeOptions("--threads", "0");
    }

    public static final class AwareFormatter implements StrictAware, ColorAware, EventListener {

        private boolean strict;
        private boolean monochrome;

        @Override
        public void setStrict(boolean strict) {
            this.strict = strict;
        }

        private boolean isStrict() {
            return strict;
        }

        @Override
        public void setMonochrome(boolean monochrome) {
            this.monochrome = monochrome;
        }

        boolean isMonochrome() {
            return monochrome;
        }

        @Override
        public void setEventPublisher(EventPublisher publisher) {

        }
    }

    @Test
    public void set_monochrome_on_color_aware_formatters() {
        RuntimeOptions options = createRuntimeOptions("--monochrome", "--plugin", AwareFormatter.class.getName());
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertTrue(formatter.isMonochrome());
    }

    @Test
    public void set_strict_on_strict_aware_formatters() {
        RuntimeOptions options = createRuntimeOptions("--strict", "--plugin", AwareFormatter.class.getName());
        Plugins plugins = new Plugins(new PluginFactory(), eventBus, options);
        AwareFormatter formatter = (AwareFormatter) plugins.getPlugins().get(0);
        assertTrue(formatter.isStrict());
    }

    @Test
    public void ensure_default_snippet_type_is_underscore() {
        Properties properties = new Properties();
        RuntimeOptions runtimeOptions = createRuntimeOptions();
        assertEquals(SnippetType.UNDERSCORE, runtimeOptions.getSnippetType());
    }

    @Test
    public void set_snippet_type() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--snippets camelcase");
        RuntimeOptions runtimeOptions = createRuntimeOptions(properties);
        assertEquals(SnippetType.CAMELCASE, runtimeOptions.getSnippetType());
    }

    private RuntimeOptions createRuntimeOptions(Properties properties, String... argv) {
        return new RuntimeOptions(resourceLoader, new Env(properties), asList(argv));
    }

    private RuntimeOptions createRuntimeOptions(String... argv) {
        return new RuntimeOptions(resourceLoader, new Env(), asList(argv));
    }

    private void assertPluginExists(List<Plugin> plugins, String pluginName) {
        assertTrue(pluginName + " not found among the plugins", pluginExists(plugins, pluginName));
    }

    private void assertPluginNotExists(List<Plugin> plugins, String pluginName) {
        assertFalse(pluginName + " found among the plugins", pluginExists(plugins, pluginName));
    }

    private boolean pluginExists(List<Plugin> plugins, String pluginName) {
        boolean found = false;
        for (Plugin plugin : plugins) {
            if (plugin.getClass().getName().equals(pluginName)) {
                found = true;
            }
        }
        return found;
    }
}
