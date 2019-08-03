package io.cucumber.core.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GluePathTest {

    @Test
    public void can_parse_empty_glue_path() {
        URI uri = GluePath.parse("");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("/"));
    }

    @Test
    public void can_parse_root_package() {
        URI uri = GluePath.parse("classpath:/");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("/"));
    }

    @Test
    public void can_parse_eclipse_plugin_default_glue() {
        // The eclipse plugin uses `classpath:` as the default
        URI uri = GluePath.parse("classpath:");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("/"));
    }

    @Test
    public void can_parse_classpath_form() {
        URI uri = GluePath.parse("classpath:com/example/app");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("com/example/app"));
    }

    @Test
    public void can_parse_relative_path_form() {
        URI uri = GluePath.parse("com/example/app");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("com/example/app"));
    }

    @Test
    public void can_parse_absolute_path_form() {
        URI uri = GluePath.parse("/com/example/app");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("/com/example/app"));
    }

    @Test
    public void can_parse_package_form() {
        URI uri = GluePath.parse("com.example.app");
        assertThat(uri.getScheme(), is("classpath"));
        assertThat(uri.getSchemeSpecificPart(), is("com/example/app"));
    }

    @Test
    public void glue_path_must_have_class_path_scheme() {
        final Executable testMethod = () -> GluePath.parse("file:com/example/app");
        final IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path must have a classpath scheme file:com/example/app"
        )));
    }

    @Test
    public void glue_path_must_have_valid_identifier_parts() {
        final Executable testMethod = () -> GluePath.parse("01-examples");
        final IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path contained invalid identifiers 01-examples"
        )));
    }

    @Test
    public void can_parse_windows_path_form() {
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = GluePath.parse("com\\example\\app");
        assertThat(uri.getScheme(), is("classpath"));
        assertEquals("com/example/app", uri.getSchemeSpecificPart());
    }

    @Test
    public void absolute_windows_path_form_is_not_valid() {
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        final Executable testMethod = () -> GluePath.parse("C:\\com\\example\\app");
        final IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path must have a classpath scheme"
        )));
    }

}
