package cucumber.runtime.java;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestStepFinished;
import cucumber.api.java.en.Given;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.Glue;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.ClasspathResourceLoader;
import gherkin.events.PickleEvent;
import gherkin.pickles.Argument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class JavaStepDefinitionTest {
    private static final Method THREE_DISABLED_MICE;
    private static final Method THREE_BLIND_ANIMALS;
    private static final String ENGLISH = "en";

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
    private final RuntimeOptions runtimeOptions = new RuntimeOptions("");
    private final Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, asList(backend), runtimeOptions);
    private final Glue glue = runtime.getGlue();
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            latestReceivedResult = event.result;
        }
    };
    private Result latestReceivedResult;

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
        runtime.getEventBus().registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
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

        PickleTag tag = new PickleTag(mock(PickleLocation.class), "@foo");
        PickleStep step = new PickleStep("three blind mice", Collections.<Argument>emptyList(), asList(mock(PickleLocation.class)));
        Pickle pickle = new Pickle("pickle name", ENGLISH, asList(step), asList(tag), asList(mock(PickleLocation.class)));
        PickleEvent pickleEvent = new PickleEvent("uri", pickle);
        runtime.getRunner().runPickle(pickleEvent, ENGLISH);

        assertEquals(AmbiguousStepDefinitionsException.class, latestReceivedResult.getError().getClass());
    }

    @Test
    public void does_not_throw_ambiguous_when_nothing_is_ambiguous() throws Throwable {
        backend.addStepDefinition(THREE_DISABLED_MICE.getAnnotation(Given.class), THREE_DISABLED_MICE);

        PickleTag tag = new PickleTag(mock(PickleLocation.class), "@foo");
        PickleStep step = new PickleStep("three blind mice", Collections.<Argument>emptyList(), asList(mock(PickleLocation.class)));
        Pickle pickle = new Pickle("pickle name", ENGLISH, asList(step), asList(tag), asList(mock(PickleLocation.class)));
        PickleEvent pickleEvent = new PickleEvent("uri", pickle);
        runtime.getRunner().runPickle(pickleEvent, ENGLISH);

        assertNull(latestReceivedResult.getError());
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
}
