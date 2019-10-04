package io.cucumber.core.options;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static io.cucumber.core.options.Constants.OPTIONS_PROPERTY_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
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

    @Test
    void loads_features_specified_in_rerun_file() throws Exception {
        mockFileResource(
            "file:path/bar.feature:2\n" +
                "file:path/foo.feature:4\n");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(
                URI.create("file:path/bar.feature"),
                URI.create("file:path/foo.feature")
            )),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/bar.feature"), singleton(2))),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/foo.feature"), singleton(4)))
        );
    }

    @Test
    void loads_no_features_when_rerun_file_is_empty() throws Exception {
        mockFileResource("");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()))
        );
    }

    @Test
    void loads_no_features_when_rerun_file_contains_new_line() throws Exception {
        mockFileResource("\n");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()))
        );
    }

    @Test
    void loads_no_features_when_rerun_file_contains_carriage_return() throws Exception {
        mockFileResource("\r");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()))
        );
    }

    @Test
    void loads_no_features_when_rerun_file_contains_new_line_and_carriage_return() throws Exception {
        mockFileResource("\r\n");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), hasSize(0)),
            () -> assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()))
        );
    }

    @Test
    void last_new_line_is_optinal() throws Exception {
        mockFileResource(
            "file:path/bar.feature:2\npath/foo.feature:4"
        );

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(URI.create("file:path/bar.feature"), URI.create("file:path/foo.feature"))),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/bar.feature"), singleton(2))),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/foo.feature"), singleton(4)))
        );
    }

    @Test
    void understands_whitespace_in_rerun_filepath() throws Exception {
        mockFileResource(
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2\n");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"))),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2)))
        );
    }

    @Test
    void understands_rerun_files_without_separation_in_rerun_filepath() throws Exception {
        mockFileResource(
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2file:/home/users/mp/My%20Documents/tests/foo.feature:4"
        );

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(runtimeOptions.getFeaturePaths(), contains(
                URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"),
                URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature")
            )),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2))),
            () -> assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature"), singleton(4)))
        );
    }


    @Test
    void loads_features_specified_in_rerun_file_with_empty_cucumber_options() throws Exception {
        mockFileResource("file:path/bar.feature:2\n");

        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getFeaturePaths(), contains(URI.create("file:path/bar.feature"))),
            () -> assertThat(options.getLineFilters(), hasEntry(URI.create("file:path/bar.feature"), singleton(2)))
        );
    }

    @Test
    void clobbers_features_from_rerun_file_specified_in_cli_if_features_specified_in_cucumber_options_property() throws Exception {
        mockFileResource("file:path/bar.feature:2\n");

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();

        RuntimeOptions options = new CucumberPropertiesParser()
            .parse(singletonMap(OPTIONS_PROPERTY_NAME, "file:path/foo.feature"))
            .build(runtimeOptions);

        assertAll("Checking RuntimeOptions",
            () -> assertThat(options.getFeaturePaths(), contains(URI.create("file:path/foo.feature"))),
            () -> assertThat(options.getLineFilters().size(), CoreMatchers.is(0))
        );
    }

    @Test
    void strips_lines_from_rerun_file_from_cli_if_filters_are_specified_in_cucumber_options_property() throws IOException {
        mockFileResource("file:path/file.feature:3\n");
        RuntimeOptions options = new CommandlineOptionsParser()
            .parse("@" + rerunPath)
            .build();
        assertThat(options.getFeaturePaths(), contains(URI.create("file:path/file.feature")));
    }

    private Map<String, String> mockFileResource(String... contents) throws IOException {
        Path path = Files.createTempFile(temp, "rerun", ".txt");
        Files.write(path, Arrays.asList(contents), UTF_8, WRITE);
        this.rerunPath = path;
        return singletonMap(OPTIONS_PROPERTY_NAME, "@" + path);
    }

}
