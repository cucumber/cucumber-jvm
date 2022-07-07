package io.cucumber.core.feature;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class GluePathTest {

    @Test
    void can_parse_empty_glue_path() {
        URI uri = GluePath.parse("");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/")));
    }

    @Test
    void can_parse_root_package() {
        URI uri = GluePath.parse("classpath:/");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/")));
    }

    @Test
    void can_parse_eclipse_plugin_default_glue() {
        // The eclipse plugin uses `classpath:` as the default
        URI uri = GluePath.parse("classpath:");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/")));
    }

    @Test
    void can_parse_classpath_form() {
        URI uri = GluePath.parse("classpath:com/example/app");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("com/example/app")));
    }

    @Test
    void can_parse_relative_path_form() {
        URI uri = GluePath.parse("com/example/app");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/com/example/app")));
    }

    @Test
    void can_parse_absolute_path_form() {
        URI uri = GluePath.parse("/com/example/app");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/com/example/app")));
    }

    @Test
    void can_parse_absolute_path_form_class() {
        URI uri = GluePath.parse("/com/example/app/Steps");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/com/example/app/Steps")));
    }

    @Test
    void can_parse_package_form() {
        URI uri = GluePath.parse("com.example.app");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/com/example/app")));
    }

    @Test
    void can_parse_package_form_class() {
        URI uri = GluePath.parse("com.example.app.Steps");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/com/example/app/Steps")));
    }

    @Test
    void glue_path_must_have_class_path_scheme() {
        Executable testMethod = () -> GluePath.parse("file:com/example/app");
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path must have a classpath scheme file:com/example/app")));
    }

    @Test
    void glue_path_must_have_valid_identifier_parts() {
        Executable testMethod = () -> GluePath.parse("01-examples");
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path contained invalid identifiers 01-examples")));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void can_parse_windows_path_form() {
        URI uri = GluePath.parse("com\\example\\app");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/com/example/app"))));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void can_parse_windows_path_form_class() {
        URI uri = GluePath.parse("com\\example\\app\\Steps");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is(equalTo("/com/example/app/Steps"))));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void absolute_windows_path_form_is_not_valid() {
        Executable testMethod = () -> GluePath.parse("C:\\com\\example\\app");
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path must have a classpath scheme C:/com/example/app")));
    }

    @ParameterizedTest
    @MethodSource("warn_when_glue_as_filesystem_path_examples")
    void when_when_glue_path_is_well_known_source_directory(String gluePath, Matcher<String> logPattern) {
        // warn when 'src/{test,main}/{java,kotlin,scala,groovy}' is used

        LogRecordListener logRecordListener = new LogRecordListener();
        LoggerFactory.addListener(logRecordListener);

        GluePath.parse(gluePath);

        LoggerFactory.removeListener(logRecordListener);

        String logMessage = logRecordListener.getLogRecords()
                .stream()
                .findFirst()
                .map(LogRecord::getMessage)
                .orElse(null);

        assertThat(logMessage, logPattern);
    }

    static Stream<Arguments> warn_when_glue_as_filesystem_path_examples() {
        return Stream.of(
            arguments("src/main/java/com/example/package",
                equalTo("" +
                        "Consider replacing glue path " +
                        "'src/main/java/com/example/package' with " +
                        "'com.example.package'.\n" +
                        "'\n" +
                        "The current glue path points to a source " +
                        "directory in your project. However cucumber " +
                        "looks for glue (i.e. step definitions) on the " +
                        "classpath. By using a package name you can " +
                        "avoid this ambiguity.")),
            arguments("src/main/java", containsString("with ''")),
            arguments("src/main/java/", containsString("with ''")),
            arguments("src/main/java_other", nullValue()),
            arguments("src/main/other", nullValue()),
            arguments("src/main/java/com", containsString("with 'com'")),
            arguments("src/main/java/com/", containsString("with 'com'")),
            arguments("src/main/groovy/com", containsString("with 'com'")),
            arguments("src/main/java/com/example", containsString("with 'com.example'")),
            arguments("src/main/java/com/example/", containsString("with 'com.example'")));
    }

}
