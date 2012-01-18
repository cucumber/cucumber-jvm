package cucumber.runtime.java;

import cucumber.annotation.en.Given;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.Glue;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeGlue;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class JavaStepDefinitionTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();
    private static final List<String> NO_PATHS = Collections.emptyList();
    private static final Method FOO;
    private static final Method BAR;

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
    private final Runtime runtime = new Runtime(NO_PATHS, new ClasspathResourceLoader(), asList(backend), false);
    private final Glue glue = new RuntimeGlue(runtime);

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
    }

    @Test(expected = AmbiguousStepDefinitionsException.class)
    public void throws_ambiguous_when_two_matches_are_found() throws Throwable {
        backend.addStepDefinition(FOO.getAnnotation(Given.class), FOO);
        backend.addStepDefinition(BAR.getAnnotation(Given.class), BAR);

        Reporter reporter = mock(Reporter.class);
        glue.buildBackendContextAndRunBeforeHooks(reporter, asSet("@foo"));
        glue.runStep("uri", new Step(NO_COMMENTS, "Given ", "pattern", 1, null, null), reporter, Locale.US);
    }

    @Test
    public void does_not_throw_ambiguous_when_nothing_is_ambiguous() throws Throwable {
        backend.addStepDefinition(FOO.getAnnotation(Given.class), FOO);

        Reporter reporter = mock(Reporter.class);
        glue.buildBackendContextAndRunBeforeHooks(reporter, asSet("@foo"));
        Step step = new Step(NO_COMMENTS, "Given ", "pattern", 1, null, null);
        glue.runStep("uri", step, reporter, Locale.US);
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

    private Set<String> asSet(String... items) {
        Set<String> set = new HashSet<String>();
        set.addAll(asList(items));
        return set;
    }
}
