package io.cucumber.java;

import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.STRICT_STUBS;

public class JavaHookTest {

    private final PickleTag pickleTagBar = new PickleTag(mock(PickleLocation.class), "@bar");
    private final PickleTag pickleTagZap = new PickleTag(mock(PickleLocation.class), "@zap");
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(STRICT_STUBS);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final Method BEFORE;
    private static final Method AFTER;
    private static final Method BEFORESTEP;
    private static final Method AFTERSTEP;
    private static final Method BAD_AFTER;
    private static final Method BAD_GENERIC_AFTER;
    private static final Method BAD_MULTIPLE;
    private static final Method SINGLE_ARG;
    private static final Method ZERO_ARG;

    static {
        try {
            BEFORE = HasHooks.class.getMethod("before");
            AFTER = HasHooks.class.getMethod("after");
            BEFORESTEP = HasHooks.class.getMethod("beforeStep");
            AFTERSTEP = HasHooks.class.getMethod("afterStep");
            BAD_AFTER = BadHook.class.getMethod("after", String.class);
            BAD_GENERIC_AFTER = BadGenericHook.class.getMethod("after", List.class);
            BAD_MULTIPLE = BadHookMultipleArgs.class.getMethod("after", Scenario.class, String.class);
            SINGLE_ARG = SingleArg.class.getMethod("after", Scenario.class);
            ZERO_ARG = ZeroArg.class.getMethod("after");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Mock
    private Glue glue;

    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final ResourceLoader resourceLoader = new MultiLoader(classLoader);
    private final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
    private final SingletonFactory objectFactory = new SingletonFactory();
    private final JavaBackend backend = new JavaBackend(objectFactory, objectFactory, classFinder);

    @org.junit.Before
    public void createBackendAndLoadNoGlue() {
        backend.loadGlue(glue, Collections.emptyList());
    }

    @Test
    public void before_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);

        verify(glue).addBeforeHook(argThat(isHookFor(BEFORE)));
    }

    private static ArgumentMatcher<JavaHookDefinition> isHookFor(final Method method) {
        return javaHookDefinition -> method.equals(javaHookDefinition.getMethod());
    }

    @Test
    public void before_step_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.addHook(BEFORESTEP.getAnnotation(BeforeStep.class), BEFORESTEP);

        verify(glue).addBeforeStepHook(argThat(isHookFor(BEFORESTEP)));
    }

    @Test
    public void after_step_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.addHook(AFTERSTEP.getAnnotation(AfterStep.class), AFTERSTEP);
        verify(glue).addAfterStepHook(argThat(isHookFor(AFTERSTEP)));
    }

    @Test
    public void after_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        verify(glue).addAfterHook(argThat(isHookFor(AFTER)));

    }

    @Test
    public void hook_order_gets_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        verify(glue).addAfterHook(argThat(isHookWithOrder(1)));

    }

    private static ArgumentMatcher<JavaHookDefinition> isHookWithOrder(final int order) {
        return argument -> argument.getOrder() == order;
    }

    @Test
    public void hook_with_no_order_is_last() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        verify(glue).addBeforeHook(argThat(isHookWithOrder(10000)));
    }

    @Test
    public void matches_matching_tags() {
        objectFactory.setInstance(new HasHooks());
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        verify(glue).addBeforeHook(argThat(isHookThatMatches(pickleTagBar, pickleTagZap)));
    }

    private static ArgumentMatcher<JavaHookDefinition> isHookThatMatches(final PickleTag... pickleTag) {
        return argument -> argument.matches(asList(pickleTag));
    }

    @Test
    public void does_not_match_non_matching_tags() {
        objectFactory.setInstance(new HasHooks());
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        verify(glue).addBeforeHook(not(argThat(isHookThatMatches(pickleTagBar))));
    }

    public static class HasHooks {

        @Before("(@foo or @bar) and @zap")
        public void before() {

        }

        @BeforeStep
        public void beforeStep() {

        }

        @AfterStep
        public void afterStep() {

        }

        @After(order = 1)
        public void after() {

        }
    }

    @Test
    public void fails_if_hook_argument_is_not_scenario_result() {
        objectFactory.setInstance(new BadHook());
        InvalidMethodSignatureException cucumberException = assertThrows(
            InvalidMethodSignatureException.class,
            () -> backend.addHook(BAD_AFTER.getAnnotation(After.class), BAD_AFTER)
        );
        assertThat(cucumberException.getMessage(), startsWith("" +
            "A method annotated with Before, After, BeforeStep or AfterStep must have one of these signatures:\n" +
            " * public void before_or_after(Scenario scenario)\n" +
            " * public void before_or_after()\n" +
            "at io.cucumber.java.JavaHookTest$BadHook.after(String) in file:"));
    }

    public static class BadHook {
        @After
        public void after(String badType) {

        }
    }

    @Test
    public void fails_if_generic_hook_argument_is_not_scenario_result() {
        objectFactory.setInstance(new BadGenericHook());
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> backend.addHook(BAD_GENERIC_AFTER.getAnnotation(After.class), BAD_GENERIC_AFTER)
        );
        assertThat(exception.getMessage(), startsWith("" +
            "A method annotated with Before, After, BeforeStep or AfterStep must have one of these signatures:\n" +
            " * public void before_or_after(Scenario scenario)\n" +
            " * public void before_or_after()\n" +
            "at io.cucumber.java.JavaHookTest$BadGenericHook.after(List<String>) in file:"));
    }

    public static class BadGenericHook {
        @After
        public void after(List<String> badType) {

        }
    }

    @Test
    public void fails_if_too_many_arguments() {
        objectFactory.setInstance(new BadGenericHook());
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> backend.addHook(BAD_MULTIPLE.getAnnotation(After.class), BAD_MULTIPLE)
        );
        assertThat(exception.getMessage(), startsWith("" +
            "A method annotated with Before, After, BeforeStep or AfterStep must have one of these signatures:\n" +
            " * public void before_or_after(Scenario scenario)\n" +
            " * public void before_or_after()\n" +
            "at io.cucumber.java.JavaHookTest$BadHookMultipleArgs.after(Scenario,String) in file:"));
    }

    public static class BadHookMultipleArgs {
        @After
        public void after(Scenario arg1, String arg2) {

        }
    }

    @Test
    public void invokes_hook_with_zero_arguments() throws Throwable {
        ZeroArg singleArg = new ZeroArg();
        SingletonFactory objectFactory = new SingletonFactory(singleArg);
        JavaHookDefinition hook = new JavaHookDefinition(ZERO_ARG, "", 0, 0, objectFactory);
        Scenario scenario = Mockito.mock(Scenario.class);
        hook.execute(scenario);
        assertTrue(objectFactory.getInstance(ZeroArg.class).invoked);
    }

    public static class ZeroArg {

        boolean invoked;

        @After
        public void after() {
            this.invoked = true;
        }
    }

    @Test
    public void invokes_hook_with_one_arguments() throws Throwable {
        SingleArg singleArg = new SingleArg();
        SingletonFactory objectFactory = new SingletonFactory(singleArg);
        JavaHookDefinition hook = new JavaHookDefinition(SINGLE_ARG, "", 0, 0, objectFactory);
        Scenario scenario = Mockito.mock(Scenario.class);
        hook.execute(scenario);
        assertThat(objectFactory.getInstance(SingleArg.class).scenario, is(scenario));
    }

    public static class SingleArg {

        Scenario scenario;

        @After
        public void after(Scenario scenario) {
            this.scenario = scenario;
        }
    }
}
