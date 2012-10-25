package cucumber.runtime.junit;

import cucumber.api.junit.Cucumber;
import cucumber.runtime.RuntimeOptions;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static cucumber.runtime.junit.RuntimeOptionsFactory.packageName;
import static cucumber.runtime.junit.RuntimeOptionsFactory.packagePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuntimeOptionsFactoryTest {
    @Test
    public void create_strict() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Strict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.strict);
    }

    @Test
    public void create_non_strict() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NotStrict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.strict);
    }

    @Test
    public void create_without_options() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptions.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.strict);
    }

    @Test
    public void create_with_no_name() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NoName.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.filters.isEmpty());
    }

    @Test
    public void create_with_multiple_names() throws Exception {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class);

        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        List<Object> filters = runtimeOptions.filters;
        assertEquals(2, filters.size());
        Iterator<Object> iterator = filters.iterator();
        assertEquals("name1", getRegexpPattern(iterator.next()));
        assertEquals("name2", getRegexpPattern(iterator.next()));
    }

    private String getRegexpPattern(Object pattern) {
        return ((Pattern) pattern).pattern();
    }

    @Test
    public void finds_path_for_class_in_package() {
        assertEquals("java/lang", packagePath(String.class));
    }

    @Test
    public void finds_path_for_class_in_toplevel_package() {
        assertEquals("", packageName("TopLevelClass"));
    }

    @Cucumber.Options(strict = true)
    static class Strict {
        // empty
    }

    @Cucumber.Options
    static class NotStrict {
        // empty
    }

    @Cucumber.Options(name = {"name1", "name2"})
    static class MultipleNames {
        // empty
    }

    @Cucumber.Options
    static class NoName {
        // empty
    }

    static class WithoutOptions {
        // empty
    }
}
