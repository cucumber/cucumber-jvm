package cucumber.runtime.io;

import io.cucumber.core.model.FeatureIdentifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResourceLoaderTest {

    @Test
    public void loads_resources_from_filesystem_dir() {
        URI uri = FeatureIdentifier.parse("src/test/resources/cucumber/runtime");
        Iterable<Resource> files = new FileResourceLoader().resources(uri, ".properties");
        assertEquals(3, toList(files).size());
    }

    @Test
    public void loads_resource_from_filesystem_file() {
        URI uri = FeatureIdentifier.parse("src/test/resources/cucumber/runtime/bar.properties");
        Iterable<Resource> files = new FileResourceLoader().resources(uri, ".doesntmatter");
        assertEquals(1, toList(files).size());
    }

    @Test
    public void loads_resources_from_jar_on_classpath() {
        URI uri = FeatureIdentifier.parse("classpath:cucumber");
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
