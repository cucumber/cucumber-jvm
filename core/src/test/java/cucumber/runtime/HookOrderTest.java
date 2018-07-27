package cucumber.runtime;

import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import io.cucumber.stepexpression.Argument;
import cucumber.api.Scenario;
import cucumber.runner.Runner;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HookOrderTest {
    private final static String ENGLISH = "en";

    private Runner runner;
    private final GlueSupplier glueSupplier = new TestGlueHelper();
    private final Glue glue = glueSupplier.get();
    private PickleEvent pickleEvent;

    @Before
    public void buildMockWorld() {
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        EventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(mock(Backend.class));
            }
        };
        PickleStep step = mock(PickleStep.class);
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.matchedArguments(step)).thenReturn(Collections.<Argument>emptyList());
        when(stepDefinition.getPattern()).thenReturn("pattern1");
        runner = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier).get();
        glue.addStepDefinition(stepDefinition);

        pickleEvent = new PickleEvent("uri", new Pickle("name", ENGLISH, asList(step), Collections.<PickleTag>emptyList(), asList(mock(PickleLocation.class))));
    }

    @Test
    public void before_hooks_execute_in_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1, -1, 0, 10000, Integer.MIN_VALUE);
        for (HookDefinition hook : hooks) {
            glue.addBeforeHook(hook);
        }

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void before_step_hooks_execute_in_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1, -1, 0, 10000, Integer.MIN_VALUE);
        for (HookDefinition hook : hooks) {
            glue.addBeforeStepHook(hook);
        }

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void after_hooks_execute_in_reverse_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0, 10000);
        for (HookDefinition hook : hooks) {
            glue.addAfterHook(hook);
        }

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void after_step_hooks_execute_in_reverse_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0, 10000);
        for (HookDefinition hook : hooks) {
            glue.addAfterStepHook(hook);
        }

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.<Scenario>any());
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
        inOrder.verify(backend1Hooks.get(2)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend2Hooks.get(0)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend1Hooks.get(0)).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend2Hooks.get(2)).execute(ArgumentMatchers.<Scenario>any());
        verify(backend2Hooks.get(1)).execute(ArgumentMatchers.<Scenario>any());
        verify(backend1Hooks.get(1)).execute(ArgumentMatchers.<Scenario>any());
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
