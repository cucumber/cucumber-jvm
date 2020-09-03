package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.plugin.DefaultSummaryPrinter;
import io.cucumber.core.plugin.HtmlFormatter;
import io.cucumber.core.plugin.NoPublishFormatter;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.plugin.PrettyFormatter;
import io.cucumber.core.plugin.ProgressFormatter;
import io.cucumber.core.plugin.PublishFormatter;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.plugin.Plugin;
import io.cucumber.tagexpressions.TagExpressionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.time.Clock;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CucumberOptionsAnnotationParserTest {

    @Test
    void create_without_options() {
        RuntimeOptions runtimeOptions = parser()
                .parse(WithoutOptions.class)
                .addDefaultSummaryPrinterIfAbsent()
                .addDefaultFormatterIfAbsent()
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getObjectFactoryClass(), is(nullValue())),
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(uri("classpath:/io/cucumber/core/options"))),
            () -> assertThat(runtimeOptions.getGlue(), contains(uri("classpath:/io/cucumber/core/options"))));

        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertAll(
            () -> assertThat(plugins.getPlugins(), hasSize(2)),
            () -> assertPluginExists(plugins.getPlugins(), ProgressFormatter.class.getName()),
            () -> assertPluginExists(plugins.getPlugins(), DefaultSummaryPrinter.class.getName()));
    }

    private CucumberOptionsAnnotationParser parser() {
        return new CucumberOptionsAnnotationParser()
                .withOptionsProvider(new CoreCucumberOptionsProvider());
    }

    public static URI uri(String str) {
        return URI.create(str);
    }

    private void assertPluginExists(List<Plugin> plugins, String pluginName) {
        boolean found = false;
        for (Plugin plugin : plugins) {
            if (plugin.getClass().getName().equals(pluginName)) {
                found = true;
                break;
            }
        }
        assertThat(pluginName + " not found among the plugins", found, is(equalTo(true)));
    }

    @Test
    void create_without_options_with_base_class_without_options() {
        Class<?> subClassWithMonoChromeTrueClass = WithoutOptionsWithBaseClassWithoutOptions.class;
        RuntimeOptions runtimeOptions = parser()
                .parse(subClassWithMonoChromeTrueClass)
                .addDefaultFormatterIfAbsent()
                .addDefaultSummaryPrinterIfAbsent()
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(uri("classpath:/io/cucumber/core/options"))),
            () -> assertThat(runtimeOptions.getGlue(), contains(uri("classpath:/io/cucumber/core/options"))),
            () -> assertThat(plugins.getPlugins(), hasSize(2)),
            () -> assertPluginExists(plugins.getPlugins(), ProgressFormatter.class.getName()),
            () -> assertPluginExists(plugins.getPlugins(), DefaultSummaryPrinter.class.getName()));
    }

    @Test
    void create_with_no_filters() {
        RuntimeOptions runtimeOptions = parser().parse(NoName.class).build();

        assertAll(
            () -> assertTrue(runtimeOptions.getTagExpressions().isEmpty()),
            () -> assertTrue(runtimeOptions.getNameFilters().isEmpty()),
            () -> assertTrue(runtimeOptions.getLineFilters().isEmpty()));
    }

    @Test
    void create_with_multiple_names() {
        RuntimeOptions runtimeOptions = parser().parse(MultipleNames.class).build();

        List<Pattern> filters = runtimeOptions.getNameFilters();
        assertThat(filters.size(), is(equalTo(2)));
        Iterator<Pattern> iterator = filters.iterator();

        assertAll(
            () -> assertThat(getRegexpPattern(iterator.next()), is(equalTo("name1"))),
            () -> assertThat(getRegexpPattern(iterator.next()), is(equalTo("name2"))));
    }

    private String getRegexpPattern(Object pattern) {
        return ((Pattern) pattern).pattern();
    }

    @Test
    void create_with_tag_expression() {
        RuntimeOptions runtimeOptions = parser().parse(TagExpression.class).build();

        List<String> tagExpressions = runtimeOptions.getTagExpressions().stream()
                .map(Object::toString)
                .collect(toList());

        assertThat(tagExpressions, contains("( @cucumber or @gherkin )"));
    }

    @Test
    void throws_runtime_exception_on_invalid_tag_with_class_location() {
        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> parser().parse(ClassWithInvalidTagExpression.class).build());

        assertAll(
            () -> assertThat(actual.getMessage(), is(
                "Invalid tag expression at 'io.cucumber.core.options.CucumberOptionsAnnotationParserTest$ClassWithInvalidTagExpression'")),
            () -> assertThat(actual.getCause(), isA(TagExpressionException.class)));
    }

    @Test
    void throws_runtime_exception_on_invalid_inherited_tag() {
        RuntimeException actual = assertThrows(RuntimeException.class,
            () -> parser().parse(ClassWithInheredInvalidTagExpression.class).build());

        assertAll(
            () -> assertThat(actual.getMessage(), is(
                "Invalid tag expression at 'io.cucumber.core.options.CucumberOptionsAnnotationParserTest$ClassWithInvalidTagExpression'")),
            () -> assertThat(actual.getCause(), isA(TagExpressionException.class)));
    }

    @Test
    void testObjectFactory() {
        RuntimeOptions runtimeOptions = parser().parse(ClassWithCustomObjectFactory.class).build();
        assertThat(runtimeOptions.getObjectFactoryClass(), is(equalTo(TestObjectFactory.class)));
    }

    @Test
    void should_set_publish_when_true() {
        RuntimeOptions runtimeOptions = parser()
                .parse(ClassWithPublish.class)
                .enablePublishPlugin()
                .build();
        assertThat(runtimeOptions.plugins(), hasSize(1));
        assertThat(runtimeOptions.plugins().get(0).pluginClass(), equalTo(PublishFormatter.class));
    }

    @Test
    void should_set_no_publish_formatter_when_plugin_option_false() {
        RuntimeOptions runtimeOptions = parser()
                .parse(WithoutOptions.class)
                .enablePublishPlugin()
                .build();
        assertThat(runtimeOptions.plugins(), hasSize(1));
        assertThat(runtimeOptions.plugins().get(0).pluginClass(), equalTo(NoPublishFormatter.class));
    }

    @Test
    void create_with_snippets() {
        RuntimeOptions runtimeOptions = parser().parse(Snippets.class).build();
        assertThat(runtimeOptions.getSnippetType(), is(equalTo(SnippetType.CAMELCASE)));
    }

    @Test
    void default_snippet_type_should_not_override_existing_snippet_type() {
        RuntimeOptions options = new RuntimeOptionsBuilder().setSnippetType(SnippetType.CAMELCASE).build();
        RuntimeOptions runtimeOptions = parser().parse(WithDefaultOptions.class).build(options);
        assertThat(runtimeOptions.getSnippetType(), is(equalTo(SnippetType.CAMELCASE)));
    }

    @Test
    void create_default_summary_printer_when_no_summary_printer_plugin_is_defined() {
        RuntimeOptions runtimeOptions = parser()
                .parse(ClassWithNoSummaryPrinterPlugin.class)
                .addDefaultSummaryPrinterIfAbsent()
                .build();
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));
        assertPluginExists(plugins.getPlugins(), DefaultSummaryPrinter.class.getName());
    }

    @Test
    void inherit_plugin_from_baseclass() {
        RuntimeOptions runtimeOptions = parser().parse(SubClassWithFormatter.class).build();
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        plugins.setEventBusOnEventListenerPlugins(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));
        List<Plugin> pluginList = plugins.getPlugins();

        assertAll(
            () -> assertPluginExists(pluginList, HtmlFormatter.class.getName()),
            () -> assertPluginExists(pluginList, PrettyFormatter.class.getName()));
    }

    @Test
    void override_monochrome_flag_from_baseclass() {
        RuntimeOptions runtimeOptions = parser().parse(SubClassWithMonoChromeTrue.class).build();

        assertTrue(runtimeOptions.isMonochrome());
    }

    @Test
    void create_with_glue() {
        RuntimeOptions runtimeOptions = parser().parse(ClassWithGlue.class).build();

        assertThat(runtimeOptions.getGlue(),
            contains(uri("classpath:/app/features/user/registration"), uri("classpath:/app/features/hooks")));
    }

    @Test
    void create_with_extra_glue() {
        RuntimeOptions runtimeOptions = parser().parse(ClassWithExtraGlue.class).build();

        assertThat(runtimeOptions.getGlue(),
            contains(uri("classpath:/app/features/hooks"), uri("classpath:/io/cucumber/core/options")));

    }

    @Test
    void create_with_extra_glue_in_subclass_of_extra_glue() {
        RuntimeOptions runtimeOptions = parser()
                .parse(SubClassWithExtraGlueOfExtraGlue.class)
                .build();

        assertThat(runtimeOptions.getGlue(),
            contains(uri("classpath:/app/features/user/hooks"), uri("classpath:/app/features/hooks"),
                uri("classpath:/io/cucumber/core/options")));
    }

    @Test
    void create_with_extra_glue_in_subclass_of_glue() {
        RuntimeOptions runtimeOptions = parser().parse(SubClassWithExtraGlueOfGlue.class).build();

        assertThat(runtimeOptions.getGlue(), contains(uri("classpath:/app/features/user/hooks"),
            uri("classpath:/app/features/user/registration"), uri("classpath:/app/features/hooks")));
    }

    @Test
    void cannot_create_with_glue_and_extra_glue() {
        Executable testMethod = () -> parser().parse(ClassWithGlueAndExtraGlue.class).build();
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(),
            is(equalTo("glue and extraGlue cannot be specified at the same time")));
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

    @CucumberOptions(name = { "name1", "name2" })
    private static class MultipleNames {
        // empty
    }

    @CucumberOptions(tags = "@cucumber or @gherkin")
    private static class TagExpression {
        // empty
    }

    @CucumberOptions(tags = "(")
    private static class ClassWithInvalidTagExpression {
        // empty
    }

    private static class ClassWithInheredInvalidTagExpression extends ClassWithInvalidTagExpression {
        // empty
    }

    @CucumberOptions
    private static class NoName {
        // empty
    }

    private static class WithoutOptions {
        // empty
    }

    @CucumberOptions
    private static class WithDefaultOptions {
        // empty
    }

    private static class WithoutOptionsWithBaseClassWithoutOptions extends WithoutOptions {
        // empty
    }

    @CucumberOptions(plugin = "pretty")
    private static class SubClassWithFormatter extends BaseClassWithFormatter {
        // empty
    }

    @CucumberOptions(plugin = "html:target/test-report.html")
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

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static class ClassWithCustomObjectFactory {
        // empty
    }

    @CucumberOptions(publish = true)
    private static class ClassWithPublish {
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

    @CucumberOptions(junit = { "option1", "option2=value" })
    private static class ClassWithJunitOption {
        // empty
    }

    @CucumberOptions(glue = { "app.features.user.registration", "app.features.hooks" })
    private static class ClassWithGlue {
        // empty
    }

    @CucumberOptions(extraGlue = "app.features.hooks")
    private static class ClassWithExtraGlue {
        // empty
    }

    @CucumberOptions(extraGlue = "app.features.user.hooks")
    private static class SubClassWithExtraGlueOfExtraGlue extends ClassWithExtraGlue {
        // empty
    }

    @CucumberOptions(extraGlue = "app.features.user.hooks")
    private static class SubClassWithExtraGlueOfGlue extends ClassWithGlue {
        // empty
    }

    @CucumberOptions(
            glue = "app.features.user.registration",
            extraGlue = "app.features.hooks"

    )
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
        public String tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
        }

        @Override
        public boolean publish() {
            return annotation.publish();
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
        public Class<? extends ObjectFactory> objectFactory() {
            return (annotation.objectFactory() == NoObjectFactory.class) ? null : annotation.objectFactory();
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

}
