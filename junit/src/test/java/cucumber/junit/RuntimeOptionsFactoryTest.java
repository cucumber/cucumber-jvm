package cucumber.junit;

import cucumber.runtime.RuntimeOptions;
import org.junit.After;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static cucumber.junit.RuntimeOptionsFactory.packageName;
import static cucumber.junit.RuntimeOptionsFactory.packagePath;
import static java.util.Arrays.asList;
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

    @Test
    public void ensure_glue_is_set_with_system_properties() {
        System.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertEquals(asList(String.format("classpath:%s", packagePath(MultipleNames.class))), runtimeOptions.glue);
    }

    @Test
    public void ensure_feature_is_set_with_system_properties() {
        System.setProperty("cucumber.options", "--tags @foo");
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertEquals(asList(String.format("classpath:%s", packagePath(MultipleNames.class))), runtimeOptions.featurePaths);
    }

    @Test
    public void override_glue_when_system_properties() {
        System.setProperty("cucumber.options", "--glue someglue");
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertEquals(asList("someglue"), runtimeOptions.glue);
    }

    @Test
    public void override_feature_path_when_system_properties() {
        System.setProperty("cucumber.options", "somepath");
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(MultipleNames.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        assertEquals(asList("somepath"), runtimeOptions.featurePaths);
    }

    @After
    public void unsetCucumberSystemProperty() {
        System.clearProperty("cucumber.options");
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
