package cucumber.runtime.java;

import cucumber.annotation.en.Given;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.Glue;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import gherkin.I18n;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JavaStepDefinitionTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();
    private static final Method THREE_DISABLED_MICE;
    private static final Method THREE_BLIND_ANIMALS;
    private static final I18n ENGLISH = new I18n("en");

    static {
        try {
            THREE_DISABLED_MICE = Defs.class.getMethod("threeDisabledMice", String.class);
            THREE_BLIND_ANIMALS = Defs.class.getMethod("threeBlindAnimals", String.class);
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    private final Defs defs = new Defs();
    private final JavaBackend backend = new JavaBackend(new SingletonFactory(defs));
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties());
    private final Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, asList(backend), runtimeOptions);
    private final Glue glue = runtime.getGlue();

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
    }

    @Test(expected = DuplicateStepDefinitionException.class)
    public void throws_duplicate_when_two_stepdefs_with_same_regexp_found() throws Throwable {
        backend.addStepDefinition(THREE_BLIND_ANIMALS.getAnnotation(Given.class), THREE_DISABLED_MICE);
        backend.addStepDefinition(THREE_BLIND_ANIMALS.getAnnotation(Given.class), THREE_BLIND_ANIMALS);
    }

    @Test
    public void throws_ambiguous_when_two_matches_are_found() throws Throwable {
        backend.addStepDefinition(THREE_DISABLED_MICE.getAnnotation(Given.class), THREE_DISABLED_MICE);
        backend.addStepDefinition(THREE_BLIND_ANIMALS.getAnnotation(Given.class), THREE_BLIND_ANIMALS);

        Reporter reporter = mock(Reporter.class);
        runtime.buildBackendWorlds(reporter);
        Tag tag = new Tag("@foo", 0);
        runtime.runBeforeHooks(reporter, asSet(tag));
        runtime.runStep("uri", new Step(NO_COMMENTS, "Given ", "three blind mice", 1, null, null), reporter, ENGLISH);

        ArgumentCaptor<Result> result = ArgumentCaptor.forClass(Result.class);
        verify(reporter).result(result.capture());
        assertEquals(AmbiguousStepDefinitionsException.class, result.getValue().getError().getClass());
    }

    @Test
    public void does_not_throw_ambiguous_when_nothing_is_ambiguous() throws Throwable {
        backend.addStepDefinition(THREE_DISABLED_MICE.getAnnotation(Given.class), THREE_DISABLED_MICE);

        Reporter reporter = new Reporter() {
            @Override
            public void before(Match match, Result result) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void result(Result result) {
                if(result.getError() != null) {
                    throw new RuntimeException(result.getError());
                }
            }

            @Override
            public void after(Match match, Result result) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void match(Match match) {
            }

            @Override
            public void embedding(String mimeType, InputStream data) {
            }

            @Override
            public void write(String text) {
            }
        };
        runtime.buildBackendWorlds(reporter);
        Tag tag = new Tag("@foo", 0);
        Set<Tag> tags = asSet(tag);
        runtime.runBeforeHooks(reporter, tags);
        Step step = new Step(NO_COMMENTS, "Given ", "three blind mice", 1, null, null);
        runtime.runStep("uri", step, reporter, ENGLISH);
        assertTrue(defs.foo);
        assertFalse(defs.bar);
    }

    public static class Defs {
        public boolean foo;
        public boolean bar;

        @Given(value = "three (.*) mice")
        public void threeDisabledMice(String disability) {
            foo = true;
        }

        @Given(value = "three blind (.*)")
        public void threeBlindAnimals(String animals) {
            bar = true;
        }
    }

    private <T> Set<T> asSet(T... items) {
        Set<T> set = new HashSet<T>();
        set.addAll(asList(items));
        return set;
    }
}
