package cucumber.runtime;

import cucumber.formatter.HTMLFormatter;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuntimeOptionsTest {
    @Test
    public void has_version_from_properties_file() {
        assertTrue(RuntimeOptions.VERSION.startsWith("1.0"));
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
        assertEquals(HTMLFormatter.class, options.formatters.get(0).getClass());
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
    public void overrides_options_with_system_properties() {
        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "--glue lookatme andmememe");
        RuntimeOptions options = new RuntimeOptions(properties, "--glue", "somewhere", "somewhere_else");
        assertEquals(asList("andmememe"), options.featurePaths);
        assertEquals(asList("lookatme"), options.glue);
    }

}
