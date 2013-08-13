package cucumber.runtime;

import cucumber.api.SnippetType;
import org.junit.Test;

import cucumber.runtime.formatter.ColorAware;
import cucumber.runtime.formatter.FormatterFactory;
import cucumber.runtime.formatter.StrictAware;

import gherkin.formatter.Formatter;

import java.net.MalformedURLException;
import java.net.URL;
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
        assertTrue(RuntimeOptions.VERSION.startsWith("1.1"));
    }

    @Test
    public void has_usage() {
        assertTrue(RuntimeOptions.USAGE.startsWith("Usage"));
    }

    @Test
    public void assigns_feature_paths() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--glue", "somewhere", "somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
    }

    @Test
    public void strips_options() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "  --glue ", "somewhere", "somewhere_else");
        assertEquals(asList("somewhere_else"), options.getFeaturePaths());
    }

    @Test
    public void assigns_glue() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--glue", "somewhere");
        assertEquals(asList("somewhere"), options.getGlue());
    }

    @Test
    public void assigns_dotcucumber() throws MalformedURLException {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--dotcucumber", "somewhere", "--glue", "somewhere");
        assertEquals(new URL("file:somewhere/"), options.getDotCucumber());
    }

    @Test
    public void creates_formatter() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--format", "html:some/dir", "--glue", "somewhere");
        assertEquals("cucumber.runtime.formatter.HTMLFormatter", options.getFormatters().get(0).getClass().getName());
    }

    @Test
    public void assigns_strict() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--strict", "--glue", "somewhere");
        assertTrue(options.isStrict());
    }

    @Test
    public void assigns_strict_short() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "-s", "--glue", "somewhere");
        assertTrue(options.isStrict());
    }

    @Test
    public void default_strict() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--glue", "somewhere");
        assertFalse(options.isStrict());
    }

    @Test
    public void name_without_spaces_is_preserved() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--name", "someName");
        Pattern actualPattern = (Pattern) options.getFilters().iterator().next();
        assertEquals("someName", actualPattern.pattern());
    }

    @Test
    public void name_with_spaces_is_preserved() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--name", "some Name");
        Pattern actualPattern = (Pattern) options.getFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void ensure_name_with_spaces_works_with_cucumber_options() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--name 'some Name'");
        RuntimeOptions options = new RuntimeOptions(properties);
        Pattern actualPattern = (Pattern) options.getFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
    }

    @Test
    public void ensure_multiple_cucumber_options_with_spaces_parse_correctly() throws MalformedURLException {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--name 'some Name' --dotcucumber 'some file\\path'");
        RuntimeOptions options = new RuntimeOptions(properties);
        Pattern actualPattern = (Pattern) options.getFilters().iterator().next();
        assertEquals("some Name", actualPattern.pattern());
        assertEquals(new URL("file:some file\\path/"), options.getDotCucumber());
    }

    @Test
    public void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--glue lookatme andmememe");
        RuntimeOptions options = new RuntimeOptions(properties, "--strict", "--glue", "somewhere", "somewhere_else");
        assertEquals(asList("somewhere_else", "andmememe"), options.getFeaturePaths());
        assertEquals(asList("somewhere", "lookatme"), options.getGlue());
        assertTrue(options.isStrict());
    }

    @Test
    public void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--glue", "somewhere");
        assertEquals(asList("somewhere"), runtimeOptions.getGlue());
    }

    @Test
    public void ensure_feature_paths_are_appended_to_when_cucumber_options_property_defined() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "somewhere_else");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "somewhere");
        assertEquals(asList("somewhere", "somewhere_else"), runtimeOptions.getFeaturePaths());
    }

    @Test
    public void clobber_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @clobber_with_this");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--tags", "@should_be_clobbered");
        assertEquals(asList("@clobber_with_this"), runtimeOptions.getFilters());
    }

    @Test
    public void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--strict");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--tags", "@keep_this");
        assertEquals(asList("@keep_this"), runtimeOptions.getFilters());
    }

    @Test
    public void allows_removal_of_strict_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--no-strict");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--strict");
        assertFalse(runtimeOptions.isStrict());
    }

    @Test
    public void fail_on_unsupported_options() {
        try {
            new RuntimeOptions(new Properties(), "-concreteUnsupportedOption", "somewhere", "somewhere_else");
            fail();
        } catch (CucumberException e) {
            assertEquals("Unknown option: -concreteUnsupportedOption", e.getMessage());
        }
    }

    @Test
    public void set_monochrome_on_color_aware_formatters() throws Exception {
        FormatterFactory factory = mock(FormatterFactory.class);
        Formatter colorAwareFormatter = mock(Formatter.class, withSettings().extraInterfaces(ColorAware.class));
        when(factory.create("progress")).thenReturn(colorAwareFormatter);

        new RuntimeOptions(new Properties(), factory, "--monochrome", "--format", "progress");

        verify((ColorAware)colorAwareFormatter).setMonochrome(true);
    }

    @Test
    public void set_strict_on_strict_aware_formatters() throws Exception {
        FormatterFactory factory = mock(FormatterFactory.class);
        Formatter strictAwareFormatter = mock(Formatter.class, withSettings().extraInterfaces(StrictAware.class));
        when(factory.create("junit:out/dir")).thenReturn(strictAwareFormatter);

        new RuntimeOptions(new Properties(), factory, "--strict", "--format", "junit:out/dir");

        verify((StrictAware)strictAwareFormatter).setStrict(true);
    }

    @Test
    public void ensure_default_snippet_type_is_underscore() {
        Properties properties = new Properties();
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties);
        assertEquals(SnippetType.UNDERSCORE, runtimeOptions.getSnippetType());
    }

    @Test
    public void set_snippet_type() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--snippets camelcase");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties);
        assertEquals(SnippetType.CAMELCASE, runtimeOptions.getSnippetType());
    }

}
