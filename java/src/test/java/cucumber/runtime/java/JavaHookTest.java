package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class JavaHookTest {

    @Test
    public void testRegisteringBeforeHook() throws Exception {
        HasHooks hasHooks = new HasHooks();
        Method beforeMethod = hasHooks.getClass().getMethod("beforeHook");

        JavaBackend backend = new JavaBackend(mock(ObjectFactory.class), null);
        backend.registerHook(beforeMethod.getAnnotation(Before.class), beforeMethod);
        JavaHookDefinition hookDef = (JavaHookDefinition) backend.getBeforeHooks().get(0);
        assertEquals(0, backend.getAfterHooks().size());
        assertEquals(beforeMethod, hookDef.getMethod());
    }

    @Test
    public void testRegisteringAfterHook() throws Exception {
        HasHooks hasHooks = new HasHooks();
        Method afterMethod = hasHooks.getClass().getMethod("afterHook");

        JavaBackend backend = new JavaBackend(mock(ObjectFactory.class), null);
        backend.registerHook(afterMethod.getAnnotation(After.class), afterMethod);
        JavaHookDefinition hookDef = (JavaHookDefinition) backend.getAfterHooks().get(0);
        assertEquals(0, backend.getBeforeHooks().size());
        assertEquals(afterMethod, hookDef.getMethod());
    }

    public class HasHooks {

        @Before
        public void beforeHook() {

        }

        @After
        public void afterHook() {

        }
    }

}
