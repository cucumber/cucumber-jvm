package cucumber.runtime;

import cucumber.io.ResourceLoader;
import gherkin.formatter.Reporter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HookOrderTest {

    private Runtime runtime;
    private Glue glue;

    @Before
    public void buildMockWorld() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        runtime = new Runtime(mock(ResourceLoader.class), new ArrayList<String>(), classLoader, asList(mock(Backend.class)), false);
        glue = runtime.getGlue();
    }

    @Test
    public void before_hooks_execute_in_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        for (HookDefinition hook : hooks) {
            glue.addBeforeHook(hook);
        }

        runtime.runBeforeHooks(mock(Reporter.class), new HashSet<String>());

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(0)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(1)).execute(Matchers.<ScenarioResult>any());
    }

    @Test
    public void after_hooks_execute_in_reverse_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(2, Integer.MAX_VALUE, 4);
        for (HookDefinition hook : hooks) {
            glue.addAfterHook(hook);
        }

        runtime.runAfterHooks(mock(Reporter.class), new HashSet<String>());

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(1)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(2)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(0)).execute(Matchers.<ScenarioResult>any());
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

        runtime.runBeforeHooks(mock(Reporter.class), new HashSet<String>());

        List<HookDefinition> allHooks = new ArrayList<HookDefinition>();
        allHooks.addAll(backend1Hooks);
        allHooks.addAll(backend2Hooks);

        InOrder inOrder = inOrder(allHooks.toArray());
        inOrder.verify(backend1Hooks.get(2)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(backend2Hooks.get(0)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(backend1Hooks.get(0)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(backend2Hooks.get(2)).execute(Matchers.<ScenarioResult>any());
        verify(backend2Hooks.get(1)).execute(Matchers.<ScenarioResult>any());
        verify(backend1Hooks.get(1)).execute(Matchers.<ScenarioResult>any());
    }

    private List<HookDefinition> mockHooks(int... ordering) {
        List<HookDefinition> hooks = new ArrayList<HookDefinition>();
        for (int order : ordering) {
            HookDefinition hook = mock(HookDefinition.class, "Mock number " + order);
            when(hook.getOrder()).thenReturn(order);
            when(hook.matches(anyListOf(String.class))).thenReturn(true);
            hooks.add(hook);
        }
        return hooks;
    }
}
