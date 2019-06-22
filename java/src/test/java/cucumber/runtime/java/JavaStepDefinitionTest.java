package cucumber.runtime.java;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestStepFinished;
import cucumber.api.java.ObjectFactory;
import cucumber.api.java.en.Given;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import cucumber.runner.AmbiguousStepDefinitionsException;
import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import io.cucumber.core.options.RuntimeOptions;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import gherkin.events.PickleEvent;
import gherkin.pickles.Argument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import io.cucumber.stepexpression.TypeRegistry;

import static java.lang.Thread.currentThread;
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
    private JavaBackend backend;
    private Result latestReceivedResult;
    private Runner runner;

    @Before
    public void createBackendAndLoadNoGlue() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ResourceLoaderClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        ObjectFactory factory = new SingletonFactory(defs);
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new JavaBackend(factory, classFinder, typeRegistry);
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        EventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return asList(backend);
            }
        };
        this.runner = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier).get();

        bus.registerHandlerFor(TestStepFinished.class, new EventHandler<TestStepFinished>() {
            @Override
            public void receive(TestStepFinished event) {
                latestReceivedResult = event.result;
            }
        });
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
        runner.runPickle(pickleEvent);

        assertEquals(AmbiguousStepDefinitionsException.class, latestReceivedResult.getError().getClass());
    }

    @Test
    public void does_not_throw_ambiguous_when_nothing_is_ambiguous() throws Throwable {
        backend.addStepDefinition(THREE_DISABLED_MICE.getAnnotation(Given.class), THREE_DISABLED_MICE);

        PickleTag tag = new PickleTag(mock(PickleLocation.class), "@foo");
        PickleStep step = new PickleStep("three blind mice", Collections.<Argument>emptyList(), asList(mock(PickleLocation.class)));
        Pickle pickle = new Pickle("pickle name", ENGLISH, asList(step), asList(tag), asList(mock(PickleLocation.class)));
        PickleEvent pickleEvent = new PickleEvent("uri", pickle);
        runner.runPickle(pickleEvent);

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
