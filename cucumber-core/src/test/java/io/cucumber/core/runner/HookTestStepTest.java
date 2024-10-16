package io.cucumber.core.runner;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.cucumber.core.backend.Status.PASSED;
import static io.cucumber.core.backend.Status.SKIPPED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HookTestStepTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    List<Object> listener = new ArrayList<>();
    private final CoreHookDefinition hookDefintion = CoreHookDefinition.create(new MockHookDefinition(listener),
        UUID::randomUUID);
    private final HookDefinitionMatch definitionMatch = new HookDefinitionMatch(hookDefintion);
    private final TestCase testCase = new TestCase(
        UUID.randomUUID(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        feature.getPickles().get(0),
        false);
    private final EventBus bus = new MockEventBus(listener);
    private final UUID testExecutionId = UUID.randomUUID();
    private final TestCaseState state = new TestCaseState(bus, testExecutionId, testCase);
    private final HookTestStep step = new HookTestStep(UUID.randomUUID(), HookType.AFTER_STEP, definitionMatch);

    @Test
    void run_does_run() {
        step.run(testCase, bus, state, ExecutionMode.RUN);

        assertInstanceOf(TestStepStarted.class, listener.get(0));
        assertEquals("HookDefinition.execute", listener.get(1));
        assertInstanceOf(TestStepFinished.class, listener.get(2));
    }

    @Test
    void run_does_dry_run() {
        step.run(testCase, bus, state, ExecutionMode.DRY_RUN);

        assertInstanceOf(TestStepStarted.class, listener.get(0));
        assertInstanceOf(TestStepFinished.class, listener.get(1));
    }

    @Test
    void next_execution_mode_is_run_when_step_passes() {
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.RUN));
        assertThat(state.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void next_execution_mode_is_skip_when_step_is_skipped() {
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.SKIP);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void next_execution_mode_is_dry_run_when_step_passes_dry_run() {
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.DRY_RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.DRY_RUN));
        assertThat(state.getStatus(), is(equalTo(PASSED)));
    }

    private static class MockHookDefinition implements HookDefinition {
        private final List<Object> listener;
        public MockHookDefinition(List<Object> listener) {
            this.listener = listener;
        }

        @Override
        public void execute(io.cucumber.core.backend.TestCaseState state) {
            listener.add("HookDefinition.execute");
        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return null;
        }
    }

    private static class MockEventBus implements EventBus {
        private final List<Object> listener;
        public MockEventBus(List<Object> listener) {
            this.listener = listener;
        }

        @Override
        public Instant getInstant() {
            return Instant.now();
        }

        @Override
        public UUID generateId() {
            return null;
        }

        @Override
        public <T> void send(T event) {
            if (!(event instanceof Envelope)) {
                listener.add(event);
            }
        }

        @Override
        public <T> void sendAll(Iterable<T> queue) {

        }

        @Override
        public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }

        @Override
        public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }
    }
}
