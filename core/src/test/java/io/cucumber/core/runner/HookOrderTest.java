package io.cucumber.core.runner;

import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.StubStepDefinition;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookOrderTest {

    private final static String ENGLISH = "en";

    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());

    private final StubStepDefinition stepDefinition = new StubStepDefinition("pattern1");
    private final PickleStep pickleStep = new PickleStep("pattern1", Collections.emptyList(), singletonList(new PickleLocation(2, 2)));
    private final PickleEvent pickleEvent = new PickleEvent("uri",
        new Pickle("scenario1", ENGLISH, singletonList(pickleStep), Collections.emptyList(), singletonList(new PickleLocation(1, 1))));

    @Test
    public void before_hooks_execute_in_order() throws Throwable {
        final List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1, -1, 0, 10000, Integer.MIN_VALUE);

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(new StubStepDefinition("pattern1"));
                for (HookDefinition hook : hooks) {
                    glue.addBeforeHook(hook);
                }

            }
        };

        runnerSupplier.get().runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.any());
    }

    @Test
    public void before_step_hooks_execute_in_order() throws Throwable {
        final List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1, -1, 0, 10000, Integer.MIN_VALUE);

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);
                for (HookDefinition hook : hooks) {
                    glue.addBeforeStepHook(hook);
                }

            }
        };

        runnerSupplier.get().runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.any());
    }

    @Test
    public void after_hooks_execute_in_reverse_order() throws Throwable {
        final List<HookDefinition> hooks = mockHooks(Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0, 10000);

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);
                for (HookDefinition hook : hooks) {
                    glue.addAfterHook(hook);
                }

            }
        };

        runnerSupplier.get().runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.any());
    }

    @Test
    public void after_step_hooks_execute_in_reverse_order() throws Throwable {
        final List<HookDefinition> hooks = mockHooks(Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0, 10000);

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);
                for (HookDefinition hook : hooks) {
                    glue.addAfterStepHook(hook);
                }

            }
        };

        runnerSupplier.get().runPickle(pickleEvent);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.any());
    }

    @Test
    public void hooks_order_across_many_backends() throws Throwable {
        final List<HookDefinition> backend1Hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        final List<HookDefinition> backend2Hooks = mockHooks(2, Integer.MAX_VALUE, 4);


        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);

                for (HookDefinition hook : backend1Hooks) {
                    glue.addBeforeHook(hook);
                }
                for (HookDefinition hook : backend2Hooks) {
                    glue.addBeforeHook(hook);
                }

            }
        };

        runnerSupplier.get().runPickle(pickleEvent);

        List<HookDefinition> allHooks = new ArrayList<>();
        allHooks.addAll(backend1Hooks);
        allHooks.addAll(backend2Hooks);

        InOrder inOrder = inOrder(allHooks.toArray());
        inOrder.verify(backend1Hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(backend2Hooks.get(0)).execute(ArgumentMatchers.any());
        inOrder.verify(backend1Hooks.get(0)).execute(ArgumentMatchers.any());
        inOrder.verify(backend2Hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(backend1Hooks.get(1)).execute(ArgumentMatchers.any());
        inOrder.verify(backend2Hooks.get(1)).execute(ArgumentMatchers.any());
    }

    private List<HookDefinition> mockHooks(int... ordering) {
        List<HookDefinition> hooks = new ArrayList<>();
        for (int order : ordering) {
            HookDefinition hook = mock(HookDefinition.class, "Mock number " + order);
            when(hook.getOrder()).thenReturn(order);
            when(hook.getTagExpression()).thenReturn("");
            hooks.add(hook);
        }
        return hooks;
    }

}
