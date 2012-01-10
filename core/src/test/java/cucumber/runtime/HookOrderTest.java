package cucumber.runtime;

import gherkin.formatter.Reporter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

public class HookOrderTest {

    private World world;

    @Before
    public void buildMockWorld() {
        world = new World(mock(Runtime.class), new ArrayList<String>());
    }

    @Test
    public void before_hooks_execute_in_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        for (HookDefinition hook : hooks) {
            world.addBeforeHook(hook);
        }

        world.buildBackendWorldsAndRunBeforeHooks(new ArrayList<String>(), mock(Reporter.class));

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(0)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(1)).execute(Matchers.<ScenarioResult>any());
    }

    @Test
    public void after_hooks_execute_in_reverse_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(2, Integer.MAX_VALUE, 4);
        for (HookDefinition hook : hooks) {
            world.addAfterHook(hook);
        }

        world.runAfterHooksAndDisposeBackendWorlds(mock(Reporter.class));

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(1)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(2)).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(hooks.get(0)).execute(Matchers.<ScenarioResult>any());
    }

    @Test
    public void hooks_order_across_many_backends() throws Throwable {
        List<HookDefinition> backend1Hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        for (HookDefinition hook : backend1Hooks) {
            world.addBeforeHook(hook);
        }
        List<HookDefinition> backend2Hooks = mockHooks(2, Integer.MAX_VALUE, 4);
        for (HookDefinition hook : backend2Hooks) {
            world.addBeforeHook(hook);
        }

        world.buildBackendWorldsAndRunBeforeHooks(new ArrayList<String>(), mock(Reporter.class));

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
            HookDefinition hook = mock(HookDefinition.class);
            when(hook.getOrder()).thenReturn(order);
            when(hook.matches(anyListOf(String.class))).thenReturn(true);
            hooks.add(hook);
        }
        return hooks;
    }
}
