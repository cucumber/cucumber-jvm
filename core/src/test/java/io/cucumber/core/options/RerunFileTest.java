package io.cucumber.core.options;

import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static io.cucumber.core.options.Env.INSTANCE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RerunFileTest {

    @Test
    public void loads_features_specified_in_rerun_file() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "path/bar.feature:2\n" +
                "path/foo.feature:4\n");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains(
            "path/bar.feature",
            "path/foo.feature"
        ));
        assertThat(runtimeOptions.getLineFilters(), equalTo(new HashMap<String, List<Long>>() {
            {
                put("path/bar.feature", singletonList(2L));
                put("path/foo.feature", singletonList(4L));
            }
        }));
    }

    @Test
    public void loads_no_features_when_rerun_file_is_empty() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            ""
        );

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "\n"
        );

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_carriage_return() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "\r");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void loads_no_features_when_rerun_file_contains_new_line_and_carriage_return() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "\r\n");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), hasSize(0));
        assertThat(runtimeOptions.getLineFilters(), equalTo(emptyMap()));
    }

    @Test
    public void last_new_line_is_optinal() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "path/bar.feature:2\npath/foo.feature:4"
        );
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains("path/bar.feature", "path/foo.feature"));
        assertThat(runtimeOptions.getLineFilters(), equalTo(new HashMap<String, List<Long>>() {
            {
                put("path/bar.feature", singletonList(2L));
                put("path/foo.feature", singletonList(4L));
            }
        }));
    }


    @Test
    public void understands_whitespace_in_rerun_filepath() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "rerun.txt",
            "/home/users/mp/My Documents/tests/bar.feature:2\n");

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains("/home/users/mp/My Documents/tests/bar.feature"));
        assertThat(runtimeOptions.getLineFilters(), equalTo(new HashMap<String, List<Long>>() {
            {
                put("/home/users/mp/My Documents/tests/bar.feature", singletonList(2L));
            }
        }));
    }


    @Test
    public void understands_rerun_files_separated_by_with_whitespace() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "/home/users/mp/My Documents/tests/bar.feature:2 /home/users/mp/My Documents/tests/foo.feature:4");
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));
        assertThat(runtimeOptions.getFeaturePaths(), contains(
            "/home/users/mp/My Documents/tests/bar.feature",
            "/home/users/mp/My Documents/tests/foo.feature"
        ));
        assertThat(runtimeOptions.getLineFilters(), equalTo(new HashMap<String, List<Long>>() {
            {
                put("/home/users/mp/My Documents/tests/bar.feature", singletonList(2L));
                put("/home/users/mp/My Documents/tests/foo.feature", singletonList(4L));
            }
        }));
    }

    @Test
    public void understands_rerun_files_without_separation_in_rerun_filepath() throws Exception {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "/home/users/mp/My Documents/tests/bar.feature:2/home/users/mp/My Documents/tests/foo.feature:4"
        );

        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, singletonList("@path/rerun.txt"));

        assertThat(runtimeOptions.getFeaturePaths(), contains(
            "/home/users/mp/My Documents/tests/bar.feature",
            "/home/users/mp/My Documents/tests/foo.feature"
        ));
        assertThat(runtimeOptions.getLineFilters(), equalTo(new HashMap<String, List<Long>>() {
            {
                put("/home/users/mp/My Documents/tests/bar.feature", singletonList(2L));
                put("/home/users/mp/My Documents/tests/foo.feature", singletonList(4L));
            }
        }));
    }

    @Test
    public void clobbers_tag_and_name_filters_from_cli_if_rerun_file_specified_in_cucumber_options_property() throws IOException {
        ResourceLoader resourceLoader = mockFileResource(
            "path/rerun.txt",
            "foo.feature:4"
        );

        Properties properties = new Properties();
        properties.setProperty("cucumber.options", "@path/rerun.txt");
        Env env = new Env(properties);
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, env,
            asList("--tags", "@should_be_clobbered", "--name", "should_be_clobbered"));

        assertEquals(Collections.emptyList(), runtimeOptions.getTagFilters());
    }

    private ResourceLoader mockFileResource(String path, String contents) throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(path);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(contents.getBytes(UTF_8)));
        when(resourceLoader.resources(path, null)).thenReturn(singletonList(resource));
        return resourceLoader;
    }

}
