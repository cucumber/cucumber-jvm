package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class JavaHookTest {
    private static final Method BEFORE;
    private static final Method AFTER;
    private static final Method BAD_AFTER;

    static {
        try {
            BEFORE = HasHooks.class.getMethod("before");
            AFTER = HasHooks.class.getMethod("after");
            BAD_AFTER = BadHook.class.getMethod("after", String.class);
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    private final SingletonFactory objectFactory = new SingletonFactory();
    private final JavaBackend backend = new JavaBackend(objectFactory);
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams(Thread.currentThread().getContextClassLoader());
    private final Glue glue = new RuntimeGlue(new UndefinedStepsTracker(), localizedXStreams);

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
    }

    @Test
    public void before_hooks_get_registered() throws Exception {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        JavaHookDefinition hookDef = (JavaHookDefinition) glue.getBeforeHooks().get(0);
        assertEquals(0, glue.getAfterHooks().size());
        assertEquals(BEFORE, hookDef.getMethod());
    }

    @Test
    public void after_hooks_get_registered() throws Exception {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        JavaHookDefinition hookDef = (JavaHookDefinition) glue.getAfterHooks().get(0);
        assertEquals(0, glue.getBeforeHooks().size());
        assertEquals(AFTER, hookDef.getMethod());
    }

    @Test
    public void hook_order_gets_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        HookDefinition hookDef = glue.getAfterHooks().get(0);
        assertEquals(1, hookDef.getOrder());
    }

    @Test
    public void hook_with_no_order_is_last() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition hookDef = glue.getBeforeHooks().get(0);
        assertEquals(10000, hookDef.getOrder());
    }

    @Test
    public void matches_matching_tags() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition before = glue.getBeforeHooks().get(0);
        assertTrue(before.matches(asList(new PickleTag(mock(PickleLocation.class), "@bar"), new PickleTag(mock(PickleLocation.class), "@zap"))));
    }

    @Test
    public void does_not_match_non_matching_tags() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition before = glue.getBeforeHooks().get(0);
        assertFalse(before.matches(asList(new PickleTag(mock(PickleLocation.class), "@bar"))));
    }

    @Test
    public void fails_if_hook_argument_is_not_scenario_result() throws Throwable {
        objectFactory.setInstance(new BadHook());
        backend.buildWorld();
        backend.addHook(BAD_AFTER.getAnnotation(After.class), BAD_AFTER);
        HookDefinition bad = glue.getAfterHooks().get(0);
        try {
            bad.execute(mock(Scenario.class));
            fail();
        } catch (CucumberException expected) {
            assertEquals("When a hook declares an argument it must be of type cucumber.api.Scenario. public void cucumber.runtime.java.JavaHookTest$BadHook.after(java.lang.String)", expected.getMessage());
        }
    }

    public static class HasHooks {

        @Before({"@foo,@bar", "@zap"})
        public void before() {

        }

        @After(order = 1)
        public void after() {

        }
    }

    public static class BadHook {
        @After
        public void after(String badType) {

        }
    }
}
