package cucumber.runtime.java;

import cucumber.annotation.en.Given;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.Glue;
import cucumber.runtime.Runtime;
import gherkin.I18n;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JavaStepDefinitionTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();
    private static final List<String> NO_PATHS = Collections.emptyList();
    private static final Method FOO;
    private static final Method BAR;
    private static final I18n ENGLISH = new I18n("en");

    static {
        try {
            FOO = Defs.class.getMethod("foo");
            BAR = Defs.class.getMethod("bar");
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    private final Defs defs = new Defs();
    private final JavaBackend backend = new JavaBackend(new SingletonFactory(defs));
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), NO_PATHS, classLoader, asList(backend), false);
    private final Glue glue = runtime.getGlue();

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
    }

    @Test
    public void throws_ambiguous_when_two_matches_are_found() throws Throwable {
        backend.addStepDefinition(FOO.getAnnotation(Given.class), Defs.class, FOO);
        backend.addStepDefinition(BAR.getAnnotation(Given.class), Defs.class, BAR);

        Reporter reporter = mock(Reporter.class);
        runtime.buildBackendWorlds();
        Tag tag = new Tag("@foo", 0);
        runtime.runBeforeHooks(reporter, asSet(tag));
        runtime.runStep("uri", new Step(NO_COMMENTS, "Given ", "pattern", 1, null, null), reporter, ENGLISH);

        ArgumentCaptor<Result> result = ArgumentCaptor.forClass(Result.class);
        verify(reporter).result(result.capture());
        assertEquals(AmbiguousStepDefinitionsException.class, result.getValue().getError().getClass());
    }

    @Test
    public void does_not_throw_ambiguous_when_nothing_is_ambiguous() throws Throwable {
        backend.addStepDefinition(FOO.getAnnotation(Given.class), Defs.class, FOO);

        Reporter reporter = mock(Reporter.class);
        runtime.buildBackendWorlds();
        Tag tag = new Tag("@foo", 0);
        Set<Tag> tags = asSet(tag);
        runtime.runBeforeHooks(reporter, tags);
        Step step = new Step(NO_COMMENTS, "Given ", "pattern", 1, null, null);
        runtime.runStep("uri", step, reporter, ENGLISH);
        assertTrue(defs.foo);
        assertFalse(defs.bar);
    }

    private class Defs {
        public boolean foo;
        public boolean bar;

        @Given(value = "pattern")
        public void foo() {
            foo = true;
        }

        @Given(value = "pattern")
        public void bar() {
            bar = true;
        }
    }

    private <T> Set<T> asSet(T... items) {
        Set<T> set = new HashSet<T>();
        set.addAll(asList(items));
        return set;
    }
}
