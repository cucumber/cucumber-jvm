package io.cucumber.core.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;

import java.io.File;
import java.net.URI;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

class FeaturePathTest {

    @Test
    void can_parse_empty_feature_path() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> FeaturePath.parse(""));
        assertThat(exception.getMessage(), is("featureIdentifier may not be empty"));
    }

    @Test
    void can_parse_root_package() {
        URI uri = FeaturePath.parse("classpath:/");
        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/")));
    }

    @Test
    void can_parse_eclipse_plugin_default_glue() {
        // The eclipse plugin uses `classpath:` as the default
        URI uri = FeaturePath.parse("classpath:");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/")));
    }

    @Test
    void can_parse_classpath_form() {
        URI uri = FeaturePath.parse("classpath:/path/to/file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/path/to/file.feature")));
    }

    @Test
    void can_parse_classpath_directory_form() {
        URI uri = FeaturePath.parse("classpath:/path/to");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/path/to")));
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void can_parse_absolute_file_form() {
        URI uri = FeaturePath.parse("file:/path/to/file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is("file")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/path/to/file.feature")));
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void can_parse_absolute_directory_form() {
        URI uri = FeaturePath.parse("file:/path/to");

        assertAll(
            () -> assertThat(uri.getScheme(), is("file")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/path/to")));
    }

    @Test
    void can_parse_relative_file_form() {
        URI uri = FeaturePath.parse("file:path/to/file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is("file")),
            () -> assertThat(uri.getSchemeSpecificPart(), endsWith("path/to/file.feature")));
    }

    @Test
    void can_parse_absolute_path_form() {
        URI uri = FeaturePath.parse("/path/to/file.feature");
        assertThat(uri.getScheme(), is(is("file")));
        // Use File to work out the drive letter on windows.
        File file = new File("/path/to/file.feature");
        assertThat(uri.getSchemeSpecificPart(), is(file.toURI().getSchemeSpecificPart()));
    }

    @Test
    void can_parse_relative_path_form() {
        URI uri = FeaturePath.parse("path/to/file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is("file")),
            () -> assertThat(uri.getSchemeSpecificPart(), endsWith("path/to/file.feature")));
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void can_parse_windows_path_form() {
        URI uri = FeaturePath.parse("path\\to\\file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is("file")),
            () -> assertThat(uri.getSchemeSpecificPart(), endsWith("path/to/file.feature")));
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void can_parse_windows_absolute_path_form() {
        URI uri = FeaturePath.parse("C:\\path\\to\\file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is(is("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/C:/path/to/file.feature")));
    }

    @Test
    void can_parse_whitespace_in_path() {
        URI uri = FeaturePath.parse("path/to the/file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is(is("file"))),
            () -> assertThat(uri.getSchemeSpecificPart(), endsWith("path/to the/file.feature")));
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void can_parse_windows_file_path_with_standard_file_separator() {
        URI uri = FeaturePath.parse("C:/path/to/file.feature");

        assertAll(
            () -> assertThat(uri.getScheme(), is("file")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/C:/path/to/file.feature")));
    }

}
