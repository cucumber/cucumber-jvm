package cucumber.runtime;

import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.resources.Consumer;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

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
        assertEquals(expected, Resources.getInstantiableSubclassesOf(Person.class, "cucumber.runtime"));
    }

    @Test
    public void looksUpFilesFromDirectoriesOnClasspath() throws IOException {
        final List<Resource> resources = new ArrayList<Resource>();
        final List<String> paths = new ArrayList<String>();
        Resources.scan("cucumber/runtime", ".properties", new Consumer() {
            public void consume(Resource resource) {
                paths.add(resource.getPath());
                resources.add(resource);
            }
        });
        assertEquals(Arrays.asList("cucumber/runtime/bar.properties", "cucumber/runtime/foo.properties"), paths);
        assertEquals("bar=BAR", resources.get(0).getString().trim());
    }

    @Test
    public void looksUpFilesOnClasspath() throws IOException {
        final List<String> paths = new ArrayList<String>();
        Resources.scan("cucumber/runtime/foo.properties", new Consumer() {
            public void consume(Resource resource) {
                paths.add(resource.getPath());
            }
        });
        assertEquals(Arrays.asList("cucumber/runtime/foo.properties"), paths);
    }

    @Test
    @Ignore
    public void looksUpFilesWithLinesOnClasspath() throws IOException {
        final List<Resource> paths = new ArrayList<Resource>();
        Resources.scan("cucumber/runtime/foo.properties:99:100", new Consumer() {
            public void consume(Resource resource) {
                paths.add(resource);
            }
        });
        assertEquals("cucumber/runtime/foo.properties", paths.get(0).getPath());
        assertEquals(asList(99L, 100L), paths.get(0).getLines());
    }
}
