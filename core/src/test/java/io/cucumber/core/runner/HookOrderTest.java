package io.cucumber.core.runner;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HookOrderTest {

    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    private final StubStepDefinition stepDefinition = new StubStepDefinition("I have 4 cukes in my belly");
    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final Pickle pickle = feature.getPickles().get(0);

    @Test
    void before_hooks_execute_in_order() {
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

        runnerSupplier.get().runPickle(pickle);

        InOrder inOrder = inOrder(hooks.toArray());
        inOrder.verify(hooks.get(6)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(3)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(4)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(2)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(0)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(5)).execute(ArgumentMatchers.any());
        inOrder.verify(hooks.get(1)).execute(ArgumentMatchers.any());
    }

    private List<HookDefinition> mockHooks(int... ordering) {
        List<HookDefinition> hooks = new ArrayList<>();
        for (int order : ordering) {
            HookDefinition hook = mock(HookDefinition.class, "Mock number " + order);
            when(hook.getOrder()).thenReturn(order);
            when(hook.getTagExpression()).thenReturn("");
            when(hook.getLocation()).thenReturn("Mock location");
            hooks.add(hook);
        }
        return hooks;
    }

    @Test
    void before_step_hooks_execute_in_order() {
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

        runnerSupplier.get().runPickle(pickle);

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
    void after_hooks_execute_in_reverse_order() {
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

        runnerSupplier.get().runPickle(pickle);

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
    void after_step_hooks_execute_in_reverse_order() {
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

        runnerSupplier.get().runPickle(pickle);

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
    void hooks_order_across_many_backends() {
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

        runnerSupplier.get().runPickle(pickle);

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

}
