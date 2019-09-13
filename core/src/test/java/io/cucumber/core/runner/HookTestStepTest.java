package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;

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

    private final CucumberFeature feature = TestFeatureParser.parse("" +
        "Feature: Test feature\n" +
        "  Scenario: Test scenario\n" +
        "     Given I have 4 cukes in my belly\n"
    );
    private final CoreHookDefinition hookDefintion = mock(CoreHookDefinition.class);
    private final HookDefinitionMatch definitionMatch = new HookDefinitionMatch(hookDefintion);
    private final TestCase testCase = new TestCase(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        feature.getPickles().get(0),
        false
    );
    private final EventBus bus = mock(EventBus.class);
    private final Scenario scenario = new Scenario(bus, testCase);
    private HookTestStep step = new HookTestStep(HookType.AFTER_STEP, definitionMatch);

    @BeforeEach
    void init() {
        Mockito.when(bus.getInstant()).thenReturn(Instant.now());
    }

    @Test
    void run_does_run() throws Throwable {
        step.run(testCase, bus, scenario, false);

        InOrder order = inOrder(bus, hookDefintion);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(hookDefintion).execute(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void run_does_dry_run() throws Throwable {
        step.run(testCase, bus, scenario, true);

        InOrder order = inOrder(bus, hookDefintion);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(hookDefintion, never()).execute(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void result_is_passed_when_step_definition_does_not_throw_exception() {
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertFalse(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void result_is_skipped_when_skip_step_is_skip_all_skipable() {
        boolean skipNextStep = step.run(testCase, bus, scenario, true);
        assertTrue(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(SKIPPED)));
    }

}
