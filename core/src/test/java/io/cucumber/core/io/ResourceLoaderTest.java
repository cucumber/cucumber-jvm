package io.cucumber.core.io;

import io.cucumber.core.feature.FeaturePath;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResourceLoaderTest {

    @Test
    public void loads_resources_from_filesystem_dir() {
        URI uri = FeaturePath.parse("src/test/resources/io/cucumber/core");
        Iterable<Resource> files = new FileResourceLoader().resources(uri, ".properties");
        assertEquals(3, toList(files).size());
    }

    @Test
    public void loads_resource_from_filesystem_file() {
        URI uri = FeaturePath.parse("src/test/resources/io/cucumber/core/bar.properties");
        Iterable<Resource> files = new FileResourceLoader().resources(uri, ".doesntmatter");
        assertEquals(1, toList(files).size());
    }

    @Test
    public void loads_resources_from_jar_on_classpath() {
        URI uri = FeaturePath.parse("classpath:io/cucumber");
        Iterable<Resource> files = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader()).resources(uri, ".properties");
        assertEquals(4, toList(files).size());
    }

    private <T> List<T> toList(Iterable<T> it) {
        List<T> result = new ArrayList<T>();
        for (T t : it) {
            result.add(t);
        }
        return result;
    }

}
