package io.cucumber.core.options;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class RerunFileTest {

    @TempDir
    Path temp;

    Path rerunPath;

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    CommandlineOptionsParser parser = new CommandlineOptionsParser(out);

    @Test
    void loads_features_specified_in_rerun_file() throws Exception {
        mockFileResource(
            "path/bar.feature:2\n" +
                    "path/foo.feature:4\n");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(
                new File("path/bar.feature").toURI(),
                new File("path/foo.feature").toURI())),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(new File("path/bar.feature").toURI(), singleton(2))),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(new File("path/foo.feature").toURI(), singleton(4))));
    }

    private void mockFileResource(String... contents) throws IOException {
        Path path = Files.createTempFile(temp, "rerun", ".txt");
        Files.write(path, Arrays.asList(contents), UTF_8, WRITE);
        this.rerunPath = path;
    }

    @Test
    void loads_no_features_when_rerun_file_is_empty() throws Exception {
        mockFileResource("");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap())));
    }

    @Test
    void loads_no_features_when_rerun_file_contains_new_line() throws Exception {
        mockFileResource("\n");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap())));
    }

    @Test
    void loads_no_features_when_rerun_file_contains_carriage_return() throws Exception {
        mockFileResource("\r");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap())));
    }

    @Test
    void loads_no_features_when_rerun_file_contains_new_line_and_carriage_return() throws Exception {
        mockFileResource("\r\n");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap())));
    }

    @Test
    void last_new_line_is_optional() throws Exception {
        mockFileResource(
            "classpath:path/bar.feature:2\nclasspath:path/foo.feature:4");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(),
                contains(URI.create("classpath:path/bar.feature"), URI.create("classpath:path/foo.feature"))),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(URI.create("classpath:path/bar.feature"), singleton(2))),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(URI.create("classpath:path/foo.feature"), singleton(4))));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void understands_whitespace_in_rerun_filepath() throws Exception {
        mockFileResource(
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2\n");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(),
                contains(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"))),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2))));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void understands_rerun_files_without_separation_in_rerun_filepath() throws Exception {
        mockFileResource(
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2file:/home/users/mp/My%20Documents/tests/foo.feature:4");

        RuntimeOptions runtimeOptions = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(
                URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"),
                URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature"))),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2))),
            () -> assertThat(runtimeOptions.getLineFilters(),
                hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature"), singleton(4))));
    }

    @Test
    void loads_features_specified_in_rerun_file_with_empty_cucumber_options() throws Exception {
        mockFileResource("file:path/bar.feature:2\n");

        RuntimeOptions options = parser
                .parse("@" + rerunPath)
                .build();

        assertAll(
            () -> assertThat(options.getFeaturePaths(), contains(new File("path/bar.feature").toURI())),
            () -> assertThat(options.getLineFilters(), hasEntry(new File("path/bar.feature").toURI(), singleton(2))));
    }

    @Test
    void strips_lines_from_rerun_file_from_cli_if_filters_are_specified_in_cucumber_options_property()
            throws IOException {
        mockFileResource("file:path/file.feature:3\n");
        RuntimeOptions options = parser
                .parse("@" + rerunPath)
                .build();
        assertThat(options.getFeaturePaths(), contains(new File("path/file.feature").toURI()));
    }

}
