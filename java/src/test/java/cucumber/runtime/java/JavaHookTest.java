package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.runtime.HookDefinition;
import org.junit.Test;

import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

    private JavaBackend backend = new JavaBackend(mock(ObjectFactory.class), null);

    @Test
    public void before_hooks_get_registered() throws Exception {
        backend.registerHook(BEFORE.getAnnotation(Before.class), BEFORE);
        JavaHookDefinition hookDef = (JavaHookDefinition) backend.getBeforeHooks().get(0);
        assertEquals(0, backend.getAfterHooks().size());
        assertEquals(BEFORE, hookDef.getMethod());
    }

    @Test
    public void after_hooks_get_registered() throws Exception {
        backend.registerHook(AFTER.getAnnotation(After.class), AFTER);
        JavaHookDefinition hookDef = (JavaHookDefinition) backend.getAfterHooks().get(0);
        assertEquals(0, backend.getBeforeHooks().size());
        assertEquals(AFTER, hookDef.getMethod());
    }

    @Test
    public void matches_matching_tags() {
        backend.registerHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition before = backend.getBeforeHooks().get(0);
        assertTrue(before.matches(asList("@bar", "@zap")));
    }

    @Test
    public void does_not_match_non_matching_tags() {
        backend.registerHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition before = backend.getBeforeHooks().get(0);
        assertFalse(before.matches(asList("@bar")));
    }

    public static class HasHooks {

        @Before({"@foo,@bar", "@zap"})
        public void before() {

        }

        @After
        public void after() {

        }
    }

}
