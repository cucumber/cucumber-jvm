package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static io.cucumber.plugin.event.HookType.AFTER_STEP;
import static io.cucumber.plugin.event.HookType.BEFORE_STEP;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class TestCaseTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n" +
            "     And I have 4 cucumber on my plate\n");

    private final EventBus bus = mock(EventBus.class);

    private final PickleStepDefinitionMatch definitionMatch1 = mock(PickleStepDefinitionMatch.class);
    private final CoreHookDefinition beforeStep1HookDefinition1 = mock(CoreHookDefinition.class);
    private final CoreHookDefinition afterStep1HookDefinition1 = mock(CoreHookDefinition.class);

    private final PickleStepTestStep testStep1 = new PickleStepTestStep(
        UUID.randomUUID(),
        URI.create("file:path/to.feature"),
        feature.getPickles().get(0).getSteps().get(0),
        singletonList(
            new HookTestStep(UUID.randomUUID(), BEFORE_STEP, new HookDefinitionMatch(beforeStep1HookDefinition1))),
        singletonList(
            new HookTestStep(UUID.randomUUID(), AFTER_STEP, new HookDefinitionMatch(afterStep1HookDefinition1))),
        definitionMatch1);

    private final PickleStepDefinitionMatch definitionMatch2 = mock(PickleStepDefinitionMatch.class);
    private final CoreHookDefinition beforeStep1HookDefinition2 = mock(CoreHookDefinition.class);
    private final CoreHookDefinition afterStep1HookDefinition2 = mock(CoreHookDefinition.class);
    private final PickleStepTestStep testStep2 = new PickleStepTestStep(
        UUID.randomUUID(),
        URI.create("file:path/to.feature"),
        feature.getPickles().get(0).getSteps().get(1),
        singletonList(
            new HookTestStep(UUID.randomUUID(), BEFORE_STEP, new HookDefinitionMatch(beforeStep1HookDefinition2))),
        singletonList(
            new HookTestStep(UUID.randomUUID(), AFTER_STEP, new HookDefinitionMatch(afterStep1HookDefinition2))),
        definitionMatch2);

    @BeforeEach
    void init() {
        when(bus.getInstant()).thenReturn(Instant.now());
        when(bus.generateId()).thenReturn(UUID.randomUUID());

        when(beforeStep1HookDefinition1.getId()).thenReturn(UUID.randomUUID());
        when(beforeStep1HookDefinition2.getId()).thenReturn(UUID.randomUUID());
        when(afterStep1HookDefinition1.getId()).thenReturn(UUID.randomUUID());
        when(afterStep1HookDefinition2.getId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void run_wraps_execute_in_test_case_started_and_finished_events() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(TestCaseState.class));

        createTestCase(testStep1).run(bus);

        InOrder order = inOrder(bus, definitionMatch1);
        order.verify(bus).send(isA(TestCaseStarted.class));
        order.verify(definitionMatch1).runStep(isA(TestCaseState.class));
        order.verify(bus).send(isA(TestCaseFinished.class));
    }

    private TestCase createTestCase(PickleStepTestStep... steps) {
        return new TestCase(UUID.randomUUID(), asList(steps), Collections.emptyList(), Collections.emptyList(),
            pickle(), false);
    }

    private Pickle pickle() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        return feature.getPickles().get(0);
    }

    @Test
    void run_all_steps() throws Throwable {
        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(definitionMatch1, definitionMatch2);
        order.verify(definitionMatch1).runStep(isA(TestCaseState.class));
        order.verify(definitionMatch2).runStep(isA(TestCaseState.class));
    }

    @Test
    void run_hooks_after_the_first_non_passed_result_for_gherkin_step() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(TestCaseState.class));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(beforeStep1HookDefinition1, definitionMatch1, afterStep1HookDefinition1);
        order.verify(beforeStep1HookDefinition1).execute(isA(TestCaseState.class));
        order.verify(definitionMatch1).runStep(isA(TestCaseState.class));
        order.verify(afterStep1HookDefinition1).execute(isA(TestCaseState.class));
    }

    @Test
    void skip_hooks_of_step_after_skipped_step() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(TestCaseState.class));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(beforeStep1HookDefinition2, definitionMatch2, afterStep1HookDefinition2);
        order.verify(beforeStep1HookDefinition2, never()).execute(isA(TestCaseState.class));
        order.verify(definitionMatch2, never()).runStep(isA(TestCaseState.class));
        order.verify(definitionMatch2, never()).dryRunStep(isA(TestCaseState.class));
        order.verify(afterStep1HookDefinition2, never()).execute(isA(TestCaseState.class));
    }

    @Test
    void skip_steps_at_first_gherkin_step_after_non_passed_result() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(TestCaseState.class));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(definitionMatch1, definitionMatch2);
        order.verify(definitionMatch1).runStep(isA(TestCaseState.class));
        order.verify(definitionMatch2, never()).dryRunStep(isA(TestCaseState.class));
        order.verify(definitionMatch2, never()).runStep(isA(TestCaseState.class));
    }

}
