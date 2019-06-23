package io.cucumber.java;

import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.java.en.Given;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runner.AmbiguousStepDefinitionsException;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.backend.DuplicateStepDefinitionException;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import gherkin.events.PickleEvent;
import gherkin.pickles.Argument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import io.cucumber.core.stepexpression.TypeRegistry;
import org.junit.jupiter.api.function.Executable;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        ObjectFactory objectFactory = new SingletonFactory(defs);
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new JavaBackend(objectFactory, objectFactory, resourceLoader);
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC());
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return asList(backend);
            }
        };
        this.runner = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, () -> objectFactory, () -> typeRegistry).get();

        bus.registerHandlerFor(TestStepFinished.class, new EventHandler<TestStepFinished>() {
            @Override
            public void receive(TestStepFinished event) {
                latestReceivedResult = event.result;
            }
        });
    }

    @Test
    public void throws_duplicate_when_two_stepdefs_with_same_regexp_found() {
        backend.addStepDefinition(THREE_BLIND_ANIMALS.getAnnotation(Given.class), THREE_DISABLED_MICE);
        final Executable testMethod = () -> backend.addStepDefinition(THREE_BLIND_ANIMALS.getAnnotation(Given.class), THREE_BLIND_ANIMALS);
        final DuplicateStepDefinitionException expectedThrown = assertThrows(DuplicateStepDefinitionException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(startsWith("Duplicate step definitions in io.cucumber.java.JavaStepDefinitionTest$Defs.threeDisabledMice(String) in file:")));
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
