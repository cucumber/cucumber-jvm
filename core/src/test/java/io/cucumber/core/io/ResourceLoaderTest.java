package io.cucumber.core.io;

import io.cucumber.core.feature.FeaturePath;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class ResourceLoaderTest {

    @Test
    void loads_resources_from_filesystem_dir() {
        URI uri = FeaturePath.parse("src/test/resources/io/cucumber/core");
        Iterable<Resource> files = new FileResourceLoader().resources(uri, ".properties");
        assertThat(toList(files).size(), is(equalTo(3)));
    }

    @Test
    void loads_resource_from_filesystem_file() {
        URI uri = FeaturePath.parse("src/test/resources/io/cucumber/core/bar.properties");
        Iterable<Resource> files = new FileResourceLoader().resources(uri, ".doesntmatter");
        assertThat(toList(files).size(), is(equalTo(1)));
    }

    @Test
    void loads_resources_from_jar_on_classpath() {
        URI uri = FeaturePath.parse("classpath:io/cucumber");
        Iterable<Resource> files = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader()).resources(uri, ".properties");
        assertThat(toList(files).size(), is(equalTo(4)));
    }

    private <T> List<T> toList(Iterable<T> it) {
        List<T> result = new ArrayList<>();
        for (T t : it) {
            result.add(t);
        }
        return result;
    }

}
