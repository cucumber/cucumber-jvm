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
import static org.junit.Assume.assumeThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FeaturePathTest {

    @Test
    public void can_parse_empty_feature_path() {
        Executable testMethod = () -> FeaturePath.parse("");
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo("featureIdentifier may not be empty")));
    }

    @Test
    public void can_parse_root_package() {
        URI uri = FeaturePath.parse("classpath:/");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/"))
        );
    }

    @Test
    public void can_parse_eclipse_plugin_default_glue() {
        // The eclipse plugin uses `classpath:` as the default
        URI uri = FeaturePath.parse("classpath:");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/"))
        );
    }

    @Test
    public void can_parse_classpath_form() {
        URI uri = FeaturePath.parse("classpath:/path/to/file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("classpath"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/path/to/file.feature")))
        );
    }

    @Test
    public void can_parse_classpath_directory_form() {
        URI uri = FeaturePath.parse("classpath:/path/to");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("classpath"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/path/to")))
        );
    }

    @Test
    public void can_parse_absolute_file_form() {
        URI uri = FeaturePath.parse("file:/path/to/file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/path/to/file.feature")))
        );
    }

    @Test
    public void can_parse_absolute_directory_form() {
        URI uri = FeaturePath.parse("file:/path/to");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/path/to")))
        );
    }

    @Test
    public void can_parse_relative_file_form() {
        URI uri = FeaturePath.parse("file:path/to/file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("path/to/file.feature")))
        );
    }

    @Test
    public void can_parse_absolute_path_form() {
        URI uri = FeaturePath.parse("/path/to/file.feature");
        assertThat(uri.getScheme(), is(equalTo("file")));
        // Use File to work out the drive letter on windows.
        File file = new File("/path/to/file.feature");
        assertThat(uri.getSchemeSpecificPart(), is(equalTo(file.toURI().getSchemeSpecificPart())));
    }

    @Test
    public void can_parse_relative_path_form() {
        URI uri = FeaturePath.parse("path/to/file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("path/to/file.feature")))
        );
    }

    @Test
    public void can_parse_windows_path_form() {
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = FeaturePath.parse("path\\to\\file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("path/to/file.feature")))
        );
    }

    @Test
    public void can_parse_windows_absolute_path_form() {
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = FeaturePath.parse("C:\\path\\to\\file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/C:/path/to/file.feature")))
        );
    }

    @Test
    public void can_parse_whitespace_in_path() {
        URI uri = FeaturePath.parse("path/to the/file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("path/to the/file.feature")))
        );
    }

    @Test
    public void can_parse_windows_file_path_with_standard_file_separator() {
        assumeThat(System.getProperty("os.name"), isWindows());

        URI uri = FeaturePath.parse("C:/path/to/file.feature");

        assertAll("Checking uri",
            () -> assertThat(uri.getScheme(), is(equalTo("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/C:/path/to/file.feature")))
        );
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
