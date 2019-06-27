package io.cucumber.core.options;

import io.cucumber.core.plugin.Plugin;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.Test;

import java.net.URI;
import java.time.Clock;
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

public class CucumberOptionsAnnotationParserTest {
    @Test
    public void create_strict() {
        RuntimeOptions runtimeOptions = parser().parse(Strict.class).build();
        assertTrue(runtimeOptions.isStrict());
    }

    private CucumberOptionsAnnotationParser parser() {
        return new CucumberOptionsAnnotationParser()
            .withOptionsProvider(new CoreCucumberOptionsProvider());
    }

    @Test
    public void create_non_strict() {
        RuntimeOptions runtimeOptions = parser().parse(NotStrict.class).build();
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void create_without_options() {
        RuntimeOptions runtimeOptions = parser()
            .parse(WithoutOptions.class)
            .addDefaultSummaryPrinterIfNotPresent()
            .addDefaultFormatterIfNotPresent()
            .build();
        assertFalse(runtimeOptions.isStrict());
        assertThat(runtimeOptions.getFeaturePaths(), contains(uri("classpath:io/cucumber/core/options")));
        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:io/cucumber/core/options")));
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertThat(plugins.getPlugins(), hasSize(2));
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.ProgressFormatter");
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    public static URI uri(String str) {
        return URI.create(str);
    }

    @Test
    public void create_without_options_with_base_class_without_options() {
        Class<?> subClassWithMonoChromeTrueClass = WithoutOptionsWithBaseClassWithoutOptions.class;
        RuntimeOptions runtimeOptions = parser()
            .parse(subClassWithMonoChromeTrueClass)
            .addDefaultFormatterIfNotPresent()
            .addDefaultSummaryPrinterIfNotPresent()
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));

        assertThat(runtimeOptions.getFeaturePaths(), contains(uri("classpath:io/cucumber/core/options")));
        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:io/cucumber/core/options")));

        assertThat(plugins.getPlugins(), hasSize(2));
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.ProgressFormatter");
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    @Test
    public void create_with_no_name() {
        RuntimeOptions runtimeOptions = parser().parse(NoName.class).build();
        assertTrue(runtimeOptions.getTagExpressions().isEmpty());
        assertTrue(runtimeOptions.getNameFilters().isEmpty());
        assertTrue(runtimeOptions.getLineFilters().isEmpty());
    }

    @Test
    public void create_with_multiple_names() {
        RuntimeOptions runtimeOptions = parser().parse(MultipleNames.class).build();

        List<Pattern> filters = runtimeOptions.getNameFilters();
        assertEquals(2, filters.size());
        Iterator<Pattern> iterator = filters.iterator();
        assertEquals("name1", getRegexpPattern(iterator.next()));
        assertEquals("name2", getRegexpPattern(iterator.next()));
    }

    @Test
    public void create_with_snippets() {
        RuntimeOptions runtimeOptions = parser().parse(Snippets.class).build();
        assertEquals(SnippetType.CAMELCASE, runtimeOptions.getSnippetType());
    }

    private String getRegexpPattern(Object pattern) {
        return ((Pattern) pattern).pattern();
    }

    @Test
    public void create_default_summary_printer_when_no_summary_printer_plugin_is_defined() {
        RuntimeOptions runtimeOptions = parser()
            .parse(ClassWithNoSummaryPrinterPlugin.class)
            .addDefaultSummaryPrinterIfNotPresent()
            .build();
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));
        assertPluginExists(plugins.getPlugins(), "io.cucumber.core.plugin.DefaultSummaryPrinter");
    }

    @Test
    public void inherit_plugin_from_baseclass() {
        RuntimeOptions runtimeOptions = parser().parse(SubClassWithFormatter.class).build();
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC()));
        List<Plugin> pluginList = plugins.getPlugins();
        assertPluginExists(pluginList, "io.cucumber.core.plugin.JSONFormatter");
        assertPluginExists(pluginList, "io.cucumber.core.plugin.PrettyFormatter");
    }

    @Test
    public void override_monochrome_flag_from_baseclass() {
        RuntimeOptions runtimeOptions = parser().parse(SubClassWithMonoChromeTrue.class).build();

        assertTrue(runtimeOptions.isMonochrome());
    }

    @Test
    public void create_with_junit_options() {
        RuntimeOptions runtimeOptions = parser().parse(ClassWithJunitOption.class).build();

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
        RuntimeOptions runtimeOptions = parser().parse(ClassWithGlue.class).build();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/user/registration"), uri("classpath:app/features/hooks")));
    }

    @Test
    public void create_with_extra_glue() {
        RuntimeOptions runtimeOptions = parser().parse(ClassWithExtraGlue.class).build();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/hooks"), uri("classpath:io/cucumber/core/options")));

    }

    @Test
    public void create_with_extra_glue_in_subclass_of_extra_glue() {
        RuntimeOptions runtimeOptions = parser()
            .parse(SubClassWithExtraGlueOfExtraGlue.class)
            .build();

        assertThat(runtimeOptions.getGlue(), 
            contains(uri("classpath:app/features/user/hooks"), uri("classpath:app/features/hooks"), uri("classpath:io/cucumber/core/options")));
    }

    @Test
    public void create_with_extra_glue_in_subclass_of_glue() {
        RuntimeOptions runtimeOptions = parser().parse(SubClassWithExtraGlueOfGlue.class).build();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:app/features/user/hooks"), uri("classpath:app/features/user/registration"), uri("classpath:app/features/hooks")));
    }

    @Test(expected = CucumberException.class)
    public void cannot_create_with_glue_and_extra_glue() {
        parser().parse(ClassWithGlueAndExtraGlue.class).build();
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

    @CucumberOptions(plugin = "io.cucumber.core.plugin.AnyStepDefinitionReporter")
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


    private static class CoreCucumberOptions implements CucumberOptionsAnnotationParser.CucumberOptions {
        private final CucumberOptions annotation;

        CoreCucumberOptions(CucumberOptions annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean dryRun() {
            return annotation.dryRun();
        }

        @Override
        public boolean strict() {
            return annotation.strict();
        }

        @Override
        public String[] features() {
            return annotation.features();
        }

        @Override
        public String[] glue() {
            return annotation.glue();
        }

        @Override
        public String[] extraGlue() {
            return annotation.extraGlue();
        }

        @Override
        public String[] tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
        }

        @Override
        public boolean monochrome() {
            return annotation.monochrome();
        }

        @Override
        public String[] name() {
            return annotation.name();
        }

        @Override
        public SnippetType snippets() {
            return annotation.snippets();
        }

        @Override
        public String[] junit() {
            return annotation.junit();
        }
    }

    private static class CoreCucumberOptionsProvider implements CucumberOptionsAnnotationParser.OptionsProvider {
        @Override
        public CucumberOptionsAnnotationParser.CucumberOptions getOptions(Class<?> clazz) {
            final CucumberOptions annotation = clazz.getAnnotation(CucumberOptions.class);
            if (annotation == null) {
                return null;
            }
            return new CoreCucumberOptions(annotation);
        }
    }
}
