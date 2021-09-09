package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static io.cucumber.core.backend.Status.PASSED;
import static io.cucumber.core.backend.Status.SKIPPED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class HookTestStepTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final CoreHookDefinition hookDefintion = mock(CoreHookDefinition.class);
    private final HookDefinitionMatch definitionMatch = new HookDefinitionMatch(hookDefintion);
    private final TestCase testCase = new TestCase(
        UUID.randomUUID(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        feature.getPickles().get(0),
        false);
    private final EventBus bus = mock(EventBus.class);
    private final UUID testExecutionId = UUID.randomUUID();
    private final TestCaseState state = new TestCaseState(bus, testExecutionId, testCase);
    private HookTestStep step = new HookTestStep(UUID.randomUUID(), HookType.AFTER_STEP, definitionMatch);

    @BeforeEach
    void init() {
        Mockito.when(bus.getInstant()).thenReturn(Instant.now());
    }

    @Test
    void run_does_run() {
        step.run(testCase, bus, state, ExecutionMode.RUN);

        InOrder order = inOrder(bus, hookDefintion);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(hookDefintion).execute(state);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void run_does_dry_run() {
        step.run(testCase, bus, state, ExecutionMode.DRY_RUN);

        InOrder order = inOrder(bus, hookDefintion);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(hookDefintion, never()).execute(state);
        order.verify(bus).send(isA(TestStepFinished.class));
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

}
