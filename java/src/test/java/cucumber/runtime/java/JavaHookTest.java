package cucumber.runtime.java;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import gherkin.formatter.model.Tag;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Test;

import cucumber.annotation.After;
import cucumber.annotation.AfterClass;
import cucumber.annotation.Before;
import cucumber.annotation.BeforeClass;
import cucumber.annotation.Order;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.converters.LocalizedXStreams;

public class JavaHookTest {
    private static final Method BEFORE;
    private static final Method AFTER;
    private static final Method BEFORECLASS;
    private static final Method AFTERCLASS;
    private static final Method INVALIDBEFORECLASS;
    private static final Method INVALIDAFTERCLASS;

    static {
        try {
            BEFORE = HasHooks.class.getMethod("before");
            AFTER = HasHooks.class.getMethod("after");
            BEFORECLASS = HasHooks.class.getMethod("beforeClass");
            AFTERCLASS = HasHooks.class.getMethod("afterClass");
            INVALIDBEFORECLASS = HasHooks.class.getMethod("invalidBeforeClass");
            INVALIDAFTERCLASS = HasHooks.class.getMethod("invalidAfterClass");
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
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        JavaHookDefinition hookDef = (JavaHookDefinition) glue.getBeforeHooks().get(0);
        assertEquals(0, glue.getAfterHooks().size());
        assertEquals(BEFORE, hookDef.getMethod());
    }

    @Test
    public void after_hooks_get_registered() throws Exception {
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        JavaHookDefinition hookDef = (JavaHookDefinition) glue.getAfterHooks().get(0);
        assertEquals(0, glue.getBeforeHooks().size());
        assertEquals(AFTER, hookDef.getMethod());
    }

    @Test
    public void hook_order_gets_registered() {
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        HookDefinition hookDef = glue.getAfterHooks().get(0);
        assertEquals(1, hookDef.getOrder());
    }

    @Test
    public void hook_with_no_order_is_last() {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition hookDef = glue.getBeforeHooks().get(0);
        assertEquals(Integer.MAX_VALUE, hookDef.getOrder());
    }

    @Test
    public void matches_matching_tags() {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition before = glue.getBeforeHooks().get(0);
        assertTrue(before.matches(asList(new Tag("@bar", 0), new Tag("@zap", 0))));
    }

    @Test
    public void does_not_match_non_matching_tags() {
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        HookDefinition before = glue.getBeforeHooks().get(0);
        assertFalse(before.matches(asList(new Tag("@bar", 0))));
    }

    @Test
    public void before_class_hooks_get_registered() throws Exception {
        backend.buildWorld();
        backend.addHook(BEFORECLASS.getAnnotation(BeforeClass.class), BEFORECLASS);
        JavaStaticHookDefinition hookDef = (JavaStaticHookDefinition) glue.getBeforeClassHooks().get(0);
        assertEquals(BEFORECLASS, hookDef.getMethod());
    }

    @Test
    public void after_class_hooks_get_registered() throws Exception {
        backend.buildWorld();
        backend.addHook(AFTERCLASS.getAnnotation(AfterClass.class), AFTERCLASS);
        JavaStaticHookDefinition hookDef = (JavaStaticHookDefinition) glue.getAfterClassHooks().get(0);
        assertEquals(AFTERCLASS, hookDef.getMethod());
    }
    @Test
    public void before_class_hooks_throw_exception_if_not_static() throws Exception {
        backend.buildWorld();
        try {
            backend.addHook(INVALIDBEFORECLASS.getAnnotation(BeforeClass.class), INVALIDBEFORECLASS);
            fail("Should throw CucumberException");
        } catch (CucumberException ce){
            //expected
        }
    }

    @Test
    public void after_class_hooks_throw_exception_if_not_static() throws Exception {
        backend.buildWorld();
        try {
            backend.addHook(INVALIDAFTERCLASS.getAnnotation(AfterClass.class), INVALIDAFTERCLASS);
            fail("Should throw CucumberException");
        } catch (CucumberException ce){
            //expected
        }

    }
    
    public static class HasHooks {

        @Before({"@foo,@bar", "@zap"})
        public void before() {

        }

        @Order(1)
        @After
        public void after() {

        }
        
        @BeforeClass
        public static void beforeClass() {}
        @AfterClass
        public static void afterClass() {}
        
        @BeforeClass
        public void invalidBeforeClass(){}
        @AfterClass
        public void invalidAfterClass(){}
    }

}
