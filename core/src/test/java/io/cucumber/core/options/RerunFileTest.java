package io.cucumber.core.options;

import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RerunFileTest {

    @Test
    public void loads_features_specified_in_rerun_file() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "file:path/bar.feature:2\n" +
                "file:path/foo.feature:4\n");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@file:path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains(
            URI.create("file:path/bar.feature"),
            URI.create("file:path/foo.feature")
        ));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/bar.feature"), singleton(2)));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/foo.feature"), singleton(4)));
    }

    @Test
    public void loads_no_features_when_rerun_file_is_empty() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            ""
        );

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "\n"
        );

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_carriage_return() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "\r");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line_and_carriage_return() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "\r\n");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void last_new_line_is_optinal() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "file:path/bar.feature:2\npath/foo.feature:4"
        );
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@file:path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains(URI.create("file:path/bar.feature"), URI.create("file:path/foo.feature")));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/bar.feature"), singleton(2)));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:path/foo.feature"), singleton(4)));
    }


    @Test
    public void understands_whitespace_in_rerun_filepath() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:rerun.txt",
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2\n");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@file:rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature")));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2)));
    }


    @Test
    public void understands_rerun_files_separated_by_with_whitespace() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2 file:/home/users/mp/My%20Documents/tests/foo.feature:4");
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@file:path/rerun.txt"));
        assertThat(runtimeOptions.getFeaturePaths(), contains(
            URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"),
            URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature")
        ));

        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2)));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature"), singleton(4)));
    }

    @Test
    public void understands_rerun_files_without_separation_in_rerun_filepath() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "file:/home/users/mp/My%20Documents/tests/bar.feature:2file:/home/users/mp/My%20Documents/tests/foo.feature:4"
        );

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@file:path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains(
            URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"),
            URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature")
        ));

        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/bar.feature"), singleton(2)));
        assertThat(runtimeOptions.getLineFilters(), hasEntry(URI.create("file:/home/users/mp/My%20Documents/tests/foo.feature"), singleton(4)));
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_rerun_file_specified_in_cucumber_options_property() throws IOException {
        ResourceLoader resourceLoader = mockFileResource(
            "file:path/rerun.txt",
            "foo.feature:4"
        );

        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "@file:path/rerun.txt");
        Env env = new Env(properties);
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, env,
            asList("--tags", "@should_be_clobbered", "--name", "should_be_clobbered"));

        assertEquals(Collections.emptyList(), runtimeOptions.getTagExpressions());
    }

    private ResourceLoader mockFileResource(String path, String contents) throws IOException {
        URI uri = URI.create(path);
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(uri);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(contents.getBytes(UTF_8)));
        when(resourceLoader.resources(uri, null)).thenReturn(singletonList(resource));
        return resourceLoader;
    }

}
