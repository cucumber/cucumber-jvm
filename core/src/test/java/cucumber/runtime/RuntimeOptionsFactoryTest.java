package cucumber.runtime;

import cucumber.api.CucumberOptions;
import cucumber.api.Plugin;
import cucumber.api.SnippetType;
import cucumber.api.formatter.Formatter;
import cucumber.runtime.io.ResourceLoader;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverters;
import cucumber.deps.com.thoughtworks.xstream.converters.basic.LongConverter;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static cucumber.runtime.RuntimeOptionsFactory.packageName;
import static cucumber.runtime.RuntimeOptionsFactory.packagePath;
import cucumber.runtime.xstream.PatternConverter;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RuntimeOptionsFactoryTest {
    @Test
    public void create_strict() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Strict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.isStrict());
    }

    @Test
    public void create_non_strict() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NotStrict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void create_without_options() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptions.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.isStrict());
        assertEquals(asList("classpath:cucumber/runtime"), runtimeOptions.getFeaturePaths());
        assertEquals(asList("classpath:cucumber/runtime"), runtimeOptions.getGlue());
        assertPluginExists(runtimeOptions.getPlugins(), "cucumber.runtime.formatter.NullFormatter");
    }

    @Test
    public void create_without_options_with_base_class_without_options() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptionsWithBaseClassWithoutOptions.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertEquals(asList("classpath:cucumber/runtime"), runtimeOptions.getFeaturePaths());
        assertEquals(asList("classpath:cucumber/runtime"), runtimeOptions.getGlue());
        assertPluginExists(runtimeOptions.getPlugins(), "cucumber.runtime.formatter.NullFormatter");
    }

    @Test
    public void create_with_no_name() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NoName.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.getTagFilters().isEmpty());
        assertTrue(runtimeOptions.getNameFilters().isEmpty());
        assertTrue(runtimeOptions.getLineFilters(mock(ResourceLoader.class)).isEmpty());
    }

    @Test
    public void create_with_multiple_names() throws Exception {
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
    public void finds_path_for_class_in_package() {
        assertEquals("java/lang", packagePath(String.class));
    }

    @Test
    public void finds_path_for_class_in_toplevel_package() {
        assertEquals("", packageName("TopLevelClass"));
    }

    @Test
    public void create_null_formatter_when_no_formatter_plugin_is_defined() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithNoFormatterPlugin.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertPluginExists(runtimeOptions.getPlugins(), "cucumber.runtime.formatter.NullFormatter");
    }

    @Test
    public void create_default_summary_printer_when_no_summary_printer_plugin_is_defined() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithNoSummaryPrinterPlugin.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertPluginExists(runtimeOptions.getPlugins(), "cucumber.runtime.DefaultSummaryPrinter");
    }

    @Test
    public void inherit_plugin_from_baseclass() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithFormatter.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<Plugin> plugins = runtimeOptions.getPlugins();
        assertPluginExists(plugins, "cucumber.runtime.formatter.JSONFormatter");
        assertPluginExists(plugins, "cucumber.runtime.formatter.PrettyFormatter");
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

    @Test
    public void create_with_xstream_converter() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithConverter.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<XStreamConverter> converters = runtimeOptions.getConverters();
        assertNotNull(converters);
        assertEquals(1, converters.size());
        assertEquals(DummyConverter.class, converters.get(0).value());
    }

    @Test
    public void create_with_xstream_converters() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(ClassWithConverters.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<XStreamConverter> converters = runtimeOptions.getConverters();
        assertNotNull(converters);
        assertEquals(2, converters.size());
        assertEquals(DummyConverter.class, converters.get(0).value());
        assertEquals(PatternConverter.class, converters.get(1).value());
    }

    @Test
    public void create_with_xstream_converter_from_baseclass() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubclassWithConverter.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<XStreamConverter> converters = runtimeOptions.getConverters();
        assertNotNull(converters);
        assertEquals(2, converters.size());
        assertEquals(LongConverter.class, converters.get(0).value());
        assertEquals(DummyConverter.class, converters.get(1).value());
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


    @CucumberOptions(snippets = SnippetType.CAMELCASE)
    static class Snippets {
        // empty
    }

    @CucumberOptions(strict = true)
    static class Strict {
        // empty
    }

    @CucumberOptions
    static class NotStrict {
        // empty
    }

    @CucumberOptions(name = {"name1", "name2"})
    static class MultipleNames {
        // empty
    }

    @CucumberOptions
    static class NoName {
        // empty
    }
    static class WithoutOptions {
        // empty
    }

    static class WithoutOptionsWithBaseClassWithoutOptions extends WithoutOptions {
        // empty
    }

    @CucumberOptions(plugin = "pretty")
    static class SubClassWithFormatter extends BaseClassWithFormatter {
        // empty
    }

    @CucumberOptions(plugin = "json:test-json-report.json")
    static class BaseClassWithFormatter {
        // empty
    }

    @CucumberOptions(monochrome = true)
    static class SubClassWithMonoChromeTrue extends BaseClassWithMonoChromeFalse {
        // empty
    }

    @CucumberOptions(monochrome = false)
    static class BaseClassWithMonoChromeFalse {
        // empty
    }

    @CucumberOptions(plugin = "cucumber.runtime.formatter.AnyStepDefinitionReporter")
    static class ClassWithNoFormatterPlugin {
        // empty
    }

    @CucumberOptions(plugin = "pretty")
    static class ClassWithNoSummaryPrinterPlugin {
        // empty
    }

    @CucumberOptions(junit = {"option1", "option2=value"})
    static class ClassWithJunitOption {
        // empty
    }

    @XStreamConverter(DummyConverter.class)
    static class ClassWithConverter {
        // empty
    }

    @XStreamConverters({
        @XStreamConverter(DummyConverter.class),
        @XStreamConverter(PatternConverter.class)
    })
    static class ClassWithConverters {
        // empty
    }

    @XStreamConverter(LongConverter.class)
    static class SubclassWithConverter extends ClassWithConverter {
        // empty
    }

}
