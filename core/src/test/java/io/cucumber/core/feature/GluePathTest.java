package io.cucumber.core.feature;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.text.IsEmptyString;
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
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
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
    void can_parse_package_form() {
        URI uri = GluePath.parse("com.example.app");

        assertAll(
            () -> assertThat(uri.getScheme(), is("classpath")),
            () -> assertThat(uri.getSchemeSpecificPart(), is("/com/example/app")));
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
    void absolute_windows_path_form_is_not_valid() {
        Executable testMethod = () -> GluePath.parse("C:\\com\\example\\app");
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "The glue path must have a classpath scheme C:/com/example/app")));
    }

    @ParameterizedTest
    @MethodSource("GluePathAndPatternProvider")
    void warn_when_glue_as_filesystem_path(String gluePath, Matcher<String> logPattern) {
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

    static Stream<Arguments> GluePathAndPatternProvider() {
        return Stream.of(
            arguments("src/main/java", containsString("for example: ''")),
            arguments("src/main/java/", containsString("for example: ''")),
            arguments("src/main/java_other", nullValue()),
            arguments("src/main/other", nullValue()),
            arguments("src/main/java/com", containsString("for example: 'com'")),
            arguments("src/main/java/com/", containsString("for example: 'com'")),
            arguments("src/main/groovy/com", containsString("for example: 'com'")),
            arguments("src/main/java/com/example", containsString("for example: 'com.example'")),
            arguments("src/main/java/com/example/", containsString("for example: 'com.example'")),
            arguments("src/main/java/com/example/package", containsString("for example: 'com.example.package'"))

        );
    }

}
