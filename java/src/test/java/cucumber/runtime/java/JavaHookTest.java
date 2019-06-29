package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;

import static java.util.Arrays.asList;
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

    static {
        try {
            BEFORE = HasHooks.class.getMethod("before");
            AFTER = HasHooks.class.getMethod("after");
            BEFORESTEP = HasHooks.class.getMethod("beforeStep");
            AFTERSTEP = HasHooks.class.getMethod("afterStep");
            BAD_AFTER = BadHook.class.getMethod("after", String.class);
        } catch (NoSuchMethodException note) {
            throw new InternalError("dang");
        }
    }

    @Mock
    private Glue glue;

    private JavaBackend backend;

    private SingletonFactory objectFactory;

    @org.junit.Before
    public void createBackendAndLoadNoGlue() {
        this.objectFactory = new SingletonFactory();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new JavaBackend(objectFactory, classFinder, typeRegistry);
        backend.loadGlue(glue, Collections.<URI>emptyList());
    }

    @Test
    public void before_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);

        verify(glue).addBeforeHook(argThat(isHookFor(BEFORE)));
    }

    private static ArgumentMatcher<JavaHookDefinition> isHookFor(final Method method) {
        return new ArgumentMatcher<JavaHookDefinition>() {
            @Override
            public boolean matches(JavaHookDefinition javaHookDefinition) {
                return method.equals(javaHookDefinition.getMethod());
            }
        };
    }

    @Test
    public void before_step_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORESTEP.getAnnotation(BeforeStep.class), BEFORESTEP);

        verify(glue).addBeforeStepHook(argThat(isHookFor(BEFORESTEP)));
    }

    @Test
    public void after_step_hooks_get_registered() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
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
        backend.buildWorld();
        backend.addHook(AFTER.getAnnotation(After.class), AFTER);
        verify(glue).addAfterHook(argThat(isHookWithOrder(1)));

    }

    private static ArgumentMatcher<JavaHookDefinition> isHookWithOrder(final int order) {
        return new ArgumentMatcher<JavaHookDefinition>() {
            @Override
            public boolean matches(JavaHookDefinition argument) {
                return argument.getOrder() == order;
            }
        };
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
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        verify(glue).addBeforeHook(argThat(isHookThatMatches(pickleTagBar, pickleTagZap)));
    }

    private static ArgumentMatcher<JavaHookDefinition> isHookThatMatches(final PickleTag... pickleTag) {
        return new ArgumentMatcher<JavaHookDefinition>() {
            @Override
            public boolean matches(JavaHookDefinition argument) {
                return argument.matches(asList(pickleTag));
            }
        };
    }

    @Test
    public void does_not_match_non_matching_tags() {
        objectFactory.setInstance(new HasHooks());
        backend.buildWorld();
        backend.addHook(BEFORE.getAnnotation(Before.class), BEFORE);
        verify(glue).addBeforeHook(not(argThat(isHookThatMatches(pickleTagBar))));
    }

    @Test
    public void fails_if_hook_argument_is_not_scenario_result() throws Throwable {
        objectFactory.setInstance(new BadHook());
        backend.buildWorld();
        backend.addHook(BAD_AFTER.getAnnotation(After.class), BAD_AFTER);

        ArgumentCaptor<JavaHookDefinition> javaHookDefinitionArgumentCaptor = ArgumentCaptor.forClass(JavaHookDefinition.class);
        verify(glue).addAfterHook(javaHookDefinitionArgumentCaptor.capture());

        HookDefinition bad = javaHookDefinitionArgumentCaptor.getValue();
        expectedException.expectMessage("When a hook declares an argument it must be of type io.cucumber.core.api.Scenario. public void cucumber.runtime.java.JavaHookTest$BadHook.after(java.lang.String)");
        bad.execute(mock(Scenario.class));
    }

    public static class HasHooks {

        @Before({"(@foo or @bar) and @zap"})
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

    public static class BadHook {
        @After
        public void after(String badType) {

        }
    }
}
