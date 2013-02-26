package cucumber.runtime.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResourceLoaderTest {
    private final File dir;

    public ResourceLoaderTest() throws UnsupportedEncodingException {
        dir = new File(URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
    }

    @Test
    public void loads_resources_from_filesystem_dir() {
        Iterable<Resource> files = new FileResourceLoader().resources(dir.getAbsolutePath(), ".properties");
        assertEquals(4, toList(files).size());
    }

    @Test
    public void loads_resource_from_filesystem_file() {
        File file = new File(dir, "cucumber/runtime/bar.properties");
        Iterable<Resource> files = new FileResourceLoader().resources(file.getPath(), ".doesntmatter");
        assertEquals(1, toList(files).size());
    }

    @Test
    public void loads_resources_from_jar_on_classpath() throws IOException {
        Iterable<Resource> files = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader()).resources("cucumber", ".properties");
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
