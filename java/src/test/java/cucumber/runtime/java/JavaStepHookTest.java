package cucumber.runtime.java;

import cucumber.api.java.AfterStep;
import cucumber.api.java.BeforeStep;
import cucumber.runtime.*;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class JavaStepHookTest {
    private static final Method BEFORE_STEP;
    private static final Method AFTER_STEP;
    private static final Method BAD_BEFORE_STEP;

    private final SingletonFactory objectFactory = new SingletonFactory();
    private final JavaBackend backend = new JavaBackend(objectFactory);
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams(Thread.currentThread().getContextClassLoader());
    private final Glue glue = new RuntimeGlue(new UndefinedStepsTracker(), localizedXStreams);

    static {
        try {
            BEFORE_STEP = HasStepHooks.class.getMethod("beforeStep");
            AFTER_STEP = HasStepHooks.class.getMethod("afterStep");
            BAD_BEFORE_STEP = HasInvalidHooks.class.getMethod("beforeStep", String.class);
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
    }

    @Test
    public void before_step_hooks_get_registered() {
        objectFactory.setInstance(new HasStepHooks());
        backend.buildWorld();
        backend.addHook(BEFORE_STEP.getAnnotation(BeforeStep.class), BEFORE_STEP);
        JavaStepHookDefinition hookDef = (JavaStepHookDefinition) glue.getBeforeHooks(HookScope.STEP).get(0);
        assertEquals(0, glue.getAfterHooks(HookScope.STEP).size());
        assertEquals(BEFORE_STEP, hookDef.getMethod());
    }

    @Test
    public void after_step_hooks_get_registered() {
        objectFactory.setInstance(new HasStepHooks());
        backend.buildWorld();
        backend.addHook(AFTER_STEP.getAnnotation(AfterStep.class), AFTER_STEP);
        JavaStepHookDefinition hookDef = (JavaStepHookDefinition) glue.getAfterHooks(HookScope.STEP).get(0);
        assertEquals(0, glue.getBeforeHooks(HookScope.STEP).size());
        assertEquals(AFTER_STEP, hookDef.getMethod());
    }

    @Test
    public void hook_order_gets_registered() {
        objectFactory.setInstance(new HasStepHooks());
        backend.buildWorld();
        backend.addHook(AFTER_STEP.getAnnotation(AfterStep.class), AFTER_STEP);
        JavaStepHookDefinition hookDef = (JavaStepHookDefinition) glue.getAfterHooks(HookScope.STEP).get(0);
        assertEquals(1, hookDef.getOrder());
    }

    @Test
    public void hook_with_no_order_is_last() {
        objectFactory.setInstance(new HasStepHooks());
        backend.buildWorld();
        backend.addHook(BEFORE_STEP.getAnnotation(BeforeStep.class), BEFORE_STEP);
        JavaStepHookDefinition hookDef = (JavaStepHookDefinition) glue.getBeforeHooks(HookScope.STEP).get(0);
        assertEquals(10000, hookDef.getOrder());
    }

    @Test
    public void fails_if_hook_argument_is_not_step() throws Throwable {
        objectFactory.setInstance(new HasInvalidHooks());
        backend.buildWorld();
        backend.addHook(BAD_BEFORE_STEP.getAnnotation(BeforeStep.class), BAD_BEFORE_STEP);
        JavaStepHookDefinition bad = (JavaStepHookDefinition) glue.getBeforeHooks(HookScope.STEP).get(0);
        try {
            bad.execute(mock(Step.class));
            fail();
        } catch (CucumberException expected) {
            assertEquals("When a hook declares an argument it must be of type gherkin.formatter.model.Step. public void cucumber.runtime.java.JavaStepHookTest$HasInvalidHooks.beforeStep(java.lang.String)", expected.getMessage());
        }
    }

    public static class HasStepHooks {
        @BeforeStep
        public void beforeStep() {
        }

        @AfterStep(order = 1)
        public void afterStep() {
        }
    }

    public static class HasInvalidHooks {
        @BeforeStep
        public void beforeStep(String badType) {
        }
    }
}