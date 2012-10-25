package cucumber.runtime;

import org.junit.Test;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals(asList("somewhere_else"), options.featurePaths);
    }

    @Test
    public void assigns_glue() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--glue", "somewhere");
        assertEquals(asList("somewhere"), options.glue);
    }

    @Test
    public void assigns_dotcucumber() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--dotcucumber", "somewhere", "--glue", "somewhere");
        assertEquals(new File("somewhere"), options.dotCucumber);
    }

    @Test
    public void creates_formatter() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--format", "html:some/dir", "--glue", "somewhere");
        assertEquals("cucumber.runtime.formatter.HTMLFormatter", options.formatters.get(0).getClass().getName());
    }

    @Test
    public void assigns_strict() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--strict", "--glue", "somewhere");
        assertTrue(options.strict);
    }

    @Test
    public void assigns_strict_short() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "-s", "--glue", "somewhere");
        assertTrue(options.strict);
    }

    @Test
    public void default_strict() {
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--glue", "somewhere");
        assertFalse(options.strict);
    }

    @Test
    public void name() {
        String someName = "someName";
        RuntimeOptions options = new RuntimeOptions(new Properties(), "--name", someName);
        Pattern actualPattern = (Pattern) options.filters.iterator().next();
        assertEquals(someName, actualPattern.pattern());
    }

    @Test
    public void name_short() {
        String someName = "someName";
        RuntimeOptions options = new RuntimeOptions(new Properties(), "-n", someName);
        Pattern actualPattern = (Pattern) options.filters.iterator().next();
        assertEquals(someName, actualPattern.pattern());
    }

    @Test
    public void overrides_options_with_system_properties_without_clobbering_non_overridden_ones() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--glue lookatme andmememe");
        RuntimeOptions options = new RuntimeOptions(properties, "--strict", "--glue", "somewhere", "somewhere_else");
        assertEquals(asList("somewhere_else", "andmememe"), options.featurePaths);
        assertEquals(asList("somewhere", "lookatme"), options.glue);
        assertTrue(options.strict);
    }

    @Test
    public void ensure_cli_glue_is_preserved_when_cucumber_options_property_defined() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--glue", "somewhere");
        assertEquals(asList("somewhere"), runtimeOptions.glue);
    }

    @Test
    public void ensure_feature_paths_are_appended_to_when_cucumber_options_property_defined() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "somewhere_else");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "somewhere");
        assertEquals(asList("somewhere", "somewhere_else"), runtimeOptions.featurePaths);
    }

    @Test
    public void clobber_filters_from_cli_if_filters_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--tags @clobber_with_this");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--tags", "@should_be_clobbered");
        assertEquals(asList("@clobber_with_this"), runtimeOptions.filters);
    }

    @Test
    public void preserves_filters_from_cli_if_filters_not_specified_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--strict");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--tags", "@keep_this");
        assertEquals(asList("@keep_this"), runtimeOptions.filters);
    }

    @Test
    public void allows_removal_of_strict_in_cucumber_options_property() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--no-strict");
        RuntimeOptions runtimeOptions = new RuntimeOptions(properties, "--strict");
        assertFalse(runtimeOptions.strict);
    }
}
