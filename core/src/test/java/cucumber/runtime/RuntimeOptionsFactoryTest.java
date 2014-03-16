package cucumber.runtime;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static cucumber.runtime.RuntimeOptionsFactory.packageName;
import static cucumber.runtime.RuntimeOptionsFactory.packagePath;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuntimeOptionsFactoryTest {
    @Test
    public void create_strict() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Strict.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.isStrict());
    }

    @Test
    public void create_non_strict() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NotStrict.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void create_without_options() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptions.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.isStrict());
        assertEquals(asList("classpath:cucumber/runtime"), runtimeOptions.getFeaturePaths());
        assertEquals(asList("classpath:cucumber/runtime"), runtimeOptions.getGlue());
    }

    @Test
    public void create_with_no_name() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NoName.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.getFilters().isEmpty());
    }

    @Test
    public void create_with_multiple_names() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class, new Class[]{CucumberOptions.class});

        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<Object> filters = runtimeOptions.getFilters();
        assertEquals(2, filters.size());
        Iterator<Object> iterator = filters.iterator();
        assertEquals("name1", getRegexpPattern(iterator.next()));
        assertEquals("name2", getRegexpPattern(iterator.next()));
    }

    @Test
    public void create_with_dotcucumber_dir() throws MalformedURLException {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(DotCucumberFile.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertEquals(new URL("file:somewhere/.cucumber/"), runtimeOptions.getDotCucumber());
    }

    @Test
    public void create_with_dotcucumber_url() throws MalformedURLException {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(DotCucumberUrl.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertEquals(new URL("https://some.where/.cucumber/"), runtimeOptions.getDotCucumber());
    }

    @Test
    public void create_with_snippets() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Snippets.class, new Class[]{CucumberOptions.class});
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
    public void inherit_formatter_from_baseclass() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithFormatter.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<Formatter> formatters = runtimeOptions.formatters();
        assertEquals(2, formatters.size());
        assertTrue(formatters.get(0) instanceof PrettyFormatter);
        assertTrue(formatters.get(1) instanceof JSONFormatter);
    }

    @Test
    public void override_monochrome_flag_from_baseclass() {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(SubClassWithMonoChromeTrue.class, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertTrue(runtimeOptions.isMonochrome());
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

    @CucumberOptions(dotcucumber = "somewhere/.cucumber")
    static class DotCucumberFile {
        // empty
    }

    @CucumberOptions(dotcucumber = "https://some.where/.cucumber")
    static class DotCucumberUrl {
        // empty
    }

    static class WithoutOptions {
        // empty
    }

    @CucumberOptions(format = "pretty")
    static class SubClassWithFormatter extends BaseClassWithFormatter {
        // empty
    }

    @CucumberOptions(format = "json:test-json-report.json")
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
}
