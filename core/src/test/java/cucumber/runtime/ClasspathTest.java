package cucumber.runtime;

import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.io.FileResource;
import cucumber.io.Resource;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClasspathTest {
    public static class Person {
    }

    public static class Fred extends Person {
    }

    public static class Wilma extends Person {
    }

    @Test
    public void looksUpInstantiableSubclassesOnClassPath() throws IOException {
        List<Class<? extends Person>> classes = Arrays.asList(Fred.class, Wilma.class);
        Set<Class<? extends Person>> expected = new HashSet<Class<? extends Person>>(classes);
        assertEquals(expected, Classpath.getInstantiableSubclassesOf(Person.class, "cucumber.runtime"));
    }

    @Test
    public void looksUpFilesByDir() throws IOException {
        final List<Resource> resources = new ArrayList<Resource>();
        final List<String> paths = new ArrayList<String>();
        Classpath.scan("cucumber/runtime", ".xyz", new Consumer() {
            public void consume(Resource resource) {
                paths.add(resource.getPath());
                resources.add(resource);
            }
        });
        assertTrue(paths.containsAll(Arrays.asList("cucumber/runtime/bar.xyz", "cucumber/runtime/foo.xyz")));
        List<String> actualResourcesString = new ArrayList<String>();
        for (Resource res : resources) {
        	actualResourcesString.add(res.getString().trim());
		}
        assertTrue(actualResourcesString.containsAll(Arrays.asList("BAR", "FOO")));
    }

    @Test
    public void looksUpFilesByFile() throws IOException {
        final List<String> paths = new ArrayList<String>();
        Classpath.scan("cucumber/runtime/foo.xyz", new Consumer() {
            public void consume(Resource resource) {
                paths.add(resource.getPath());
            }
        });
        assertEquals(Arrays.asList("cucumber/runtime/foo.xyz"), paths);
    }
}
