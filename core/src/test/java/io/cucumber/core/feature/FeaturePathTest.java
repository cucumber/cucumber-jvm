package io.cucumber.core.feature;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FeaturePathTest {

    @Test
    public void can_parse_empty_feature_path() {
        final Executable testMethod = () -> FeaturePath.parse("");
        final IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo("featureIdentifier may not be empty")));
   }

    @Test
    public void can_parse_root_package() {
        URI uri = FeaturePath.parse("classpath:/");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("/"));
    }

    @Test
    public void can_parse_eclipse_plugin_default_glue() {
        // The eclipse plugin uses `classpath:` as the default
        URI uri = FeaturePath.parse("classpath:");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("/"));
    }

    @Test
    public void can_parse_classpath_form() {
        URI uri = FeaturePath.parse("classpath:/path/to/file.feature");
        assertEquals("classpath", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_classpath_directory_form() {
        URI uri = FeaturePath.parse("classpath:/path/to");
        assertEquals("classpath", uri.getScheme());
        assertEquals("/path/to", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_absolute_file_form() {
        URI uri = FeaturePath.parse("file:/path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_absolute_directory_form() {
        URI uri = FeaturePath.parse("file:/path/to");
        assertEquals("file", uri.getScheme());
        assertEquals("/path/to", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_relative_file_form() {
        URI uri = FeaturePath.parse("file:path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_absolute_path_form() {
        URI uri = FeaturePath.parse("/path/to/file.feature");
        assertEquals("file", uri.getScheme());
        // Use File to work out the drive letter on windows.
        File file = new File("/path/to/file.feature");
        assertEquals(file.toURI().getSchemeSpecificPart(), uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_relative_path_form() {
        URI uri = FeaturePath.parse("path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_windows_path_form() {
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = FeaturePath.parse("path\\to\\file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_windows_absolute_path_form() {
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = FeaturePath.parse("C:\\path\\to\\file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("/C:/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_whitespace_in_path() {
        URI uri = FeaturePath.parse("path/to the/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to the/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_windows_file_path_with_standard_file_separator() {
        assumeThat(System.getProperty("os.name"), isWindows());

        URI uri = FeaturePath.parse("C:/path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("/C:/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    private static Matcher<String> isWindows() {
        return new CustomTypeSafeMatcher<String>("windows") {
            @Override
            protected boolean matchesSafely(String value) {
                if (value == null) {
                    return false;
                }
                return value
                    .toLowerCase(Locale.US)
                    .replaceAll("[^a-z0-9]+", "")
                    .contains("windows");
            }
        };
    }

}
