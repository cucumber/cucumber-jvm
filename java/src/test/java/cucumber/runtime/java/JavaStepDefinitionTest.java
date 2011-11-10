package cucumber.runtime.java;

import cucumber.annotation.en.Given;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class JavaStepDefinitionTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();
    private static final List<String> NO_PATHS = Collections.emptyList();
    private static final Method FOO;
    private static final Method FOO_OR_BAR;
    private static final Method NOT_FOO;

    static {
        try {
            FOO = Defs.class.getMethod("foo");
            FOO_OR_BAR = Defs.class.getMethod("foo_or_bar");
            NOT_FOO = Defs.class.getMethod("not_foo");
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    private final Defs defs = new Defs();
    private final JavaBackend backend = new JavaBackend(new SingletonFactory(defs));
    private final Runtime runtime = new Runtime(NO_PATHS, asList(backend), false);
    private final World fooWorld = new World(runtime, asList("@foo"));

    @Test(expected = AmbiguousStepDefinitionsException.class)
    public void throws_ambiguous_when_two_matches_are_found() {
        backend.buildWorld(new ArrayList<String>(), fooWorld);
        backend.addStepDefinition(FOO.getAnnotation(Given.class), FOO);
        backend.addStepDefinition(FOO_OR_BAR.getAnnotation(Given.class), FOO_OR_BAR);

        Reporter reporter = mock(Reporter.class);
        fooWorld.prepare(NO_PATHS);
        fooWorld.runStep("uri", new Step(NO_COMMENTS, "Given ", "pattern", 1, null, null), reporter, Locale.US);
    }

    public void does_not_throw_ambiguous_when_tags_cause_only_one_to_match() {
        backend.buildWorld(new ArrayList<String>(), fooWorld);
        backend.addStepDefinition(FOO.getAnnotation(Given.class), FOO);
        backend.addStepDefinition(NOT_FOO.getAnnotation(Given.class), NOT_FOO);

        Reporter reporter = mock(Reporter.class);
        fooWorld.prepare(NO_PATHS);
        fooWorld.runStep("uri", new Step(NO_COMMENTS, "Given ", "pattern", 1, null, null), reporter, Locale.US);
        assertTrue(defs.foo);
        assertFalse(defs.not_foo);
    }

    private class Defs {
        public boolean foo;
        public boolean foo_or_bar;
        public boolean not_foo;

        @Given(value = "pattern", tags = {"@foo"})
        public void foo() {
            foo = true;
        }

        @Given(value = "pattern", tags = {"@foo,@bar"})
        public void foo_or_bar() {
            foo_or_bar = true;
        }

        @Given(value = "pattern", tags = {"~@foo"})
        public void not_foo() {
            not_foo  = true;
        }
    }
}
