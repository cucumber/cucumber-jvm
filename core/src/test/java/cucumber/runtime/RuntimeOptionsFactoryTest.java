package cucumber.runtime;

import cucumber.api.CucumberOptions;
import cucumber.api.Plugin;
import cucumber.api.SnippetType;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import org.junit.Test;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RuntimeOptionsFactoryTest {
    @Test
    public void create_strict() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Strict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.isStrict());
    }

    @Test
    public void create_non_strict() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NotStrict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void create_without_options() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptions.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.isStrict());
        assertThat(runtimeOptions.getFeaturePaths(), contains(uri("classpath:cucumber/runtime")));
        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:cucumber/runtime")));
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(plugins.getPlugins(), hasSize(2));
        assertPluginExists(plugins.getPlugins(), "cucumber.runtime.formatter.ProgressFormatter");
        assertPluginExists(plugins.getPlugins(), "cucumber.runtime.formatter.DefaultSummaryPrinter");
    }

    public static URI uri(String str) {
        return URI.create(str);
    }

    @Test
    public void create_without_options_with_base_class_without_options() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptionsWithBaseClassWithoutOptions.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));

        assertThat(runtimeOptions.getFeaturePaths(), contains(uri("classpath:cucumber/runtime")));
        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:cucumber/runtime")));

        assertThat(plugins.getPlugins(), hasSize(2));
        assertPluginExists(plugins.getPlugins(), "cucumber.runtime.formatter.ProgressFormatter");
        assertPluginExists(plugins.getPlugins(), "cucumber.runtime.formatter.DefaultSummaryPrinter");
    }

    @Test
    public void create_with_no_name() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NoName.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.getTagFilters().isEmpty());
        assertTrue(runtimeOptions.getNameFilters().isEmpty());
        assertTrue(runtimeOptions.getLineFilters().isEmpty());
    }

    @Test
    public void create_with_multiple_names() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class);

        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<Pattern> filters = runtimeOptions.getNameFilters();
        assertEquals(2, filters.size());
        Iterator<Pattern> iterator = filters.iterator();
        assertEquals("name1", getRegexpPattern(iterator.next()));
        assertEquals("name2", getRegexpPattern(iterator.next()));
    }

    @Test
    public void create_with_snippets() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Snippets.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertEquals(SnippetType.CAMELCASE, runtimeOptions.getSnippetType());
    }

    private String getRegexpPattern(Object pattern) {
        return ((Pattern) pattern).pattern();
    }

    @Test
    public void create_default_summary_printer_when_no_summary_printer_plugin_is_defined() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithNoSummaryPrinterPlugin.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));
        assertPluginExists(plugins.getPlugins(), "cucumber.runtime.formatter.DefaultSummaryPrinter");
    }

    @Test
    public void inherit_plugin_from_baseclass() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithFormatter.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        Plugins plugins = new Plugins(getClass().getClassLoader(), new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(TimeService.SYSTEM));
        List<Plugin> pluginList = plugins.getPlugins();
        assertPluginExists(pluginList, "cucumber.runtime.formatter.JSONFormatter");
        assertPluginExists(pluginList, "cucumber.runtime.formatter.PrettyFormatter");
    }

    @Test
    public void override_monochrome_flag_from_baseclass() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithMonoChromeTrue.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertTrue(runtimeOptions.isMonochrome());
    }

    @Test
    public void create_with_junit_options() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithJunitOption.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertEquals(asList("option1", "option2=value"), runtimeOptions.getJunitOptions());
    }

    private void assertPluginExists(List<Plugin> plugins, String pluginName) {
        boolean found = false;
        for (Plugin plugin : plugins) {
            if (plugin.getClass().getName().equals(pluginName)) {
                found = true;
            }
        }
        assertTrue(pluginName + " not found among the plugins", found);
    }

    @Test
    public void create_with_glue() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithGlue.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/user/registration"), uri("classpath:app/features/hooks")));
    }

    @Test
    public void create_with_extra_glue() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithExtraGlue.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/hooks"), uri("classpath:cucumber/runtime")));

    }

    @Test
    public void create_with_extra_glue_in_subclass_of_extra_glue() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithExtraGlueOfExtraGlue.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/user/hooks"), uri("classpath:app/features/hooks"), uri("classpath:cucumber/runtime")));
    }

    @Test
    public void create_with_extra_glue_in_subclass_of_glue() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithExtraGlueOfGlue.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/user/hooks"), uri("classpath:app/features/user/registration"), uri("classpath:app/features/hooks")));
    }

    @Test(expected = CucumberException.class)
    public void cannot_create_with_glue_and_extra_glue() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithGlueAndExtraGlue.class);
        runtimeOptionsFactory.create();
    }


    @CucumberOptions(snippets = SnippetType.CAMELCASE)
    private static class Snippets {
        // empty
    }

    @CucumberOptions(strict = true)
    private static class Strict {
        // empty
    }

    @CucumberOptions
    private static class NotStrict {
        // empty
    }

    @CucumberOptions(name = {"name1", "name2"})
    private static class MultipleNames {
        // empty
    }

    @CucumberOptions
    private static class NoName {
        // empty
    }
    private static class WithoutOptions {
        // empty
    }

    private static class WithoutOptionsWithBaseClassWithoutOptions extends WithoutOptions {
        // empty
    }

    @CucumberOptions(plugin = "pretty")
    private static class SubClassWithFormatter extends BaseClassWithFormatter {
        // empty
    }

    @CucumberOptions(plugin = "json:target/test-json-report.json")
    private static class BaseClassWithFormatter {
        // empty
    }

    @CucumberOptions(monochrome = true)
    private static class SubClassWithMonoChromeTrue extends BaseClassWithMonoChromeFalse {
        // empty
    }

    @CucumberOptions(monochrome = false)
    private static class BaseClassWithMonoChromeFalse {
        // empty
    }

    @CucumberOptions(plugin = "cucumber.runtime.formatter.AnyStepDefinitionReporter")
    private static class ClassWithNoFormatterPlugin {
        // empty
    }

    @CucumberOptions(plugin = "pretty")
    private static class ClassWithNoSummaryPrinterPlugin {
        // empty
    }

    @CucumberOptions(junit = {"option1", "option2=value"})
    private static class ClassWithJunitOption {
        // empty
    }

    @CucumberOptions(glue = {"app.features.user.registration", "app.features.hooks"})
    private static class ClassWithGlue {
        // empty
    }

    @CucumberOptions(extraGlue = {"app.features.hooks"})
    private static class ClassWithExtraGlue {
        // empty
    }

    @CucumberOptions(extraGlue = {"app.features.user.hooks"})
    private static class SubClassWithExtraGlueOfExtraGlue extends ClassWithExtraGlue {
        // empty
    }

    @CucumberOptions(extraGlue = {"app.features.user.hooks"})
    private static class SubClassWithExtraGlueOfGlue extends ClassWithGlue {
        // empty
    }

    @CucumberOptions(extraGlue = {"app.features.hooks"}, glue = {"app.features.user.registration"})
    private static class ClassWithGlueAndExtraGlue {
        // empty
    }

}
