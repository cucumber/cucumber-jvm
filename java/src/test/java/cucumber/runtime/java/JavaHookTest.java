package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.model.Tag;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class JavaHookTest {
    private static final Method BEFORE;
    private static final Method AFTER;

    static {
        try {
            BEFORE = HasHooks.class.getMethod("before");
            AFTER = HasHooks.class.getMethod("after");
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    private final JavaBackend backend = new JavaBackend(mock(ObjectFactory.class));
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams(Thread.currentThread().getContextClassLoader());
    private final Glue glue = new RuntimeGlue(new UndefinedStepsTracker(), localizedXStreams);

    @org.junit.Before
    public void loadNoGlue() {
        backend.loadGlue(glue, Collections.<String>emptyList());
    }

    @Test
    public void before_hooks_get_registered() throws Exception {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), HasHooks.class, BEFORE);
        JavaHookDefinition hookDef = (JavaHookDefinition) glue.getBeforeHooks().get(0);
        assertEquals(0, glue.getAfterHooks().size());
        assertEquals(BEFORE, hookDef.getMethod());
    }

    @Test
    public void after_hooks_get_registered() throws Exception {
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), HasHooks.class, AFTER);
        JavaHookDefinition hookDef = (JavaHookDefinition) glue.getAfterHooks().get(0);
        assertEquals(0, glue.getBeforeHooks().size());
        assertEquals(AFTER, hookDef.getMethod());
    }

    @Test
    public void hook_order_gets_registered() {
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), HasHooks.class, AFTER);
        HookDefinition hookDef = glue.getAfterHooks().get(0);
        assertEquals(1, hookDef.getOrder());
    }

    @Test
    public void hook_with_no_order_is_last() {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), HasHooks.class, BEFORE);
        HookDefinition hookDef = glue.getBeforeHooks().get(0);
        assertEquals(Integer.MAX_VALUE, hookDef.getOrder());
    }

    @Test
    public void matches_matching_tags() {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), HasHooks.class, BEFORE);
        HookDefinition before = glue.getBeforeHooks().get(0);
        assertTrue(before.matches(asList(new Tag("@bar", 0), new Tag("@zap", 0))));
    }

    @Test
    public void does_not_match_non_matching_tags() {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), HasHooks.class, BEFORE);
        HookDefinition before = glue.getBeforeHooks().get(0);
        assertFalse(before.matches(asList(new Tag("@bar", 0))));
    }

    public static class HasHooks {

        @Before({"@foo,@bar", "@zap"})
        public void before() {

        }

        @Order(1)
        @After
        public void after() {

        }
    }

}
