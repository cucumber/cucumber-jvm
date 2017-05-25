package cucumber.runtime;

import cucumber.api.Scenario;
import cucumber.runner.Runner;
import cucumber.runtime.io.ResourceLoader;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HookOrderTest {
    private final static String ENGLISH = "en";

    private Runner runner;
    private Glue glue;
    private PickleEvent pickleEvent;

    @Before
    public void buildMockWorld() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        Runtime runtime = new Runtime(mock(ResourceLoader.class), classLoader, asList(mock(Backend.class)), runtimeOptions);
        runner = runtime.getRunner();
        glue = runtime.getGlue();
        pickleEvent = new PickleEvent("uri", new Pickle("name", ENGLISH, Collections.<PickleStep>emptyList(), Collections.<PickleTag>emptyList(), asList(mock(PickleLocation.class))));
    }

    @Test
    public void before_hooks_execute_in_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1, -1, 0, 10000, Integer.MIN_VALUE);
        for (HookDefinition hook : hooks) {
            glue.addBeforeHook(hook);
        }

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(6)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(3)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(4)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(2)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(0)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(5)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(1)).execute(Matchers.<Scenario>any());
    }

    @Test
    public void after_hooks_execute_in_reverse_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0, 10000);
        for (HookDefinition hook : hooks) {
            glue.addAfterHook(hook);
        }

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(6)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(3)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(1)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(5)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(4)).execute(Matchers.<Scenario>any());
        inOrder.verify(hooks.get(0)).execute(Matchers.<Scenario>any());
    }

    @Test
    public void hooks_order_across_many_backends() throws Throwable {
        List<HookDefinition> backend1Hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        for (HookDefinition hook : backend1Hooks) {
            glue.addBeforeHook(hook);
        }
        List<HookDefinition> backend2Hooks = mockHooks(2, Integer.MAX_VALUE, 4);
        for (HookDefinition hook : backend2Hooks) {
            glue.addBeforeHook(hook);
        }

        runner.runPickle(pickleEvent);

        List<HookDefinition> allHooks = new ArrayList<HookDefinition>();
        allHooks.addAll(backend1Hooks);
        allHooks.addAll(backend2Hooks);

        InOrder inOrder = inOrder(allHooks.toArray());
        inOrder.verify(backend1Hooks.get(2)).execute(Matchers.<Scenario>any());
        inOrder.verify(backend2Hooks.get(0)).execute(Matchers.<Scenario>any());
        inOrder.verify(backend1Hooks.get(0)).execute(Matchers.<Scenario>any());
        inOrder.verify(backend2Hooks.get(2)).execute(Matchers.<Scenario>any());
        verify(backend2Hooks.get(1)).execute(Matchers.<Scenario>any());
        verify(backend1Hooks.get(1)).execute(Matchers.<Scenario>any());
    }

    private List<HookDefinition> mockHooks(int... ordering) {
        List<HookDefinition> hooks = new ArrayList<HookDefinition>();
        for (int order : ordering) {
            HookDefinition hook = mock(HookDefinition.class, "Mock number " + order);
            when(hook.getOrder()).thenReturn(order);
            when(hook.matches(anyListOf(PickleTag.class))).thenReturn(true);
            hooks.add(hook);
        }
        return hooks;
    }
}
