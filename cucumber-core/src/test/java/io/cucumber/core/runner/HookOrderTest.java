package io.cucumber.core.runner;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HookOrderTest {

    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    private final StubStepDefinition stepDefinition = new StubStepDefinition("I have 4 cukes in my belly");
    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final Pickle pickle = feature.getPickles().get(0);
    private final List<HookDefinition> listener = new ArrayList<>();

    @Test
    void before_hooks_execute_in_order() {
        final List<HookDefinition> hooks = mockHooks(listener, 3, Integer.MAX_VALUE, 1, -1, 0, 10000,
            Integer.MIN_VALUE);

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

        verifyHookDefinitionExecutedInOrder();
    }

    private void verifyHookDefinitionExecutedInOrder() {
        long previousOrder = Long.MIN_VALUE;
        for (HookDefinition hd : listener) {
            assertTrue(hd.getOrder() >= previousOrder);
            previousOrder = hd.getOrder();
        }
    }

    private void verifyHookDefinitionExecutedInReverseOrder() {
        long previousOrder = Long.MAX_VALUE;
        for (HookDefinition hd : listener) {
            assertTrue(hd.getOrder() <= previousOrder);
            previousOrder = hd.getOrder();
        }
    }

    private List<HookDefinition> mockHooks(List<HookDefinition> listener, int... ordering) {
        List<HookDefinition> hooks = new ArrayList<>();
        for (int order : ordering) {
            hooks.add(new MockHookDefinition(order, listener));
        }
        return hooks;
    }

    @Test
    void before_step_hooks_execute_in_order() {
        final List<HookDefinition> hooks = mockHooks(listener, 3, Integer.MAX_VALUE, 1, -1, 0, 10000,
            Integer.MIN_VALUE);

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

        verifyHookDefinitionExecutedInOrder();
    }

    @Test
    void after_hooks_execute_in_reverse_order() {
        final List<HookDefinition> hooks = mockHooks(listener, Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0,
            10000);

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

        verifyHookDefinitionExecutedInReverseOrder();
    }

    @Test
    void after_step_hooks_execute_in_reverse_order() {
        final List<HookDefinition> hooks = mockHooks(listener, Integer.MIN_VALUE, 2, Integer.MAX_VALUE, 4, -1, 0,
            10000);

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

        verifyHookDefinitionExecutedInReverseOrder();
    }

    @Test
    void hooks_order_across_many_backends() {
        final List<HookDefinition> backend1Hooks = mockHooks(listener, 3, Integer.MAX_VALUE, 1);
        final List<HookDefinition> backend2Hooks = mockHooks(listener, 2, Integer.MAX_VALUE, 4);

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

        verifyHookDefinitionExecutedInOrder();
    }

    private static class MockHookDefinition implements HookDefinition {
        private final int order;
        private final List<HookDefinition> listener;

        public MockHookDefinition(int order, List<HookDefinition> listener) {
            this.order = order;
            this.listener = listener;
        }

        @Override
        public void execute(TestCaseState state) {
            listener.add(this);
        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "Mock location";
        }
    }
}
