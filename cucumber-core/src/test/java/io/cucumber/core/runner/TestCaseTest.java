package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.cucumber.plugin.event.HookType.AFTER_STEP;
import static io.cucumber.plugin.event.HookType.BEFORE_STEP;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCaseTest {
    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n" +
            "     And I have 4 cucumber on my plate\n");

    private final PickleStepTestStep testStep1 = createPickleStepTestStep(0,
        new PickleStepDefinitionMatch(Collections.emptyList(),
            new StubStepDefinition(),
            null,
            null));

    private final PickleStepTestStep testStep2 = createPickleStepTestStep(1,
        new PickleStepDefinitionMatch(Collections.emptyList(),
            new StubStepDefinition(),
            null,
            null));

    private final PickleStepTestStep testStepUndefined = createPickleStepTestStep(0,
        new UndefinedPickleStepDefinitionMatch(null, null));

    private PickleStepTestStep createPickleStepTestStep(int index, PickleStepDefinitionMatch definitionMatch) {
        return new PickleStepTestStep(
            UUID.randomUUID(),
            URI.create("file:path/to.feature"),
            feature.getPickles().get(0).getSteps().get(index),
            singletonList(
                new HookTestStep(UUID.randomUUID(), BEFORE_STEP,
                    new HookDefinitionMatch(CoreHookDefinition.create(new StubHookDefinition(), UUID::randomUUID)))),
            singletonList(
                new HookTestStep(UUID.randomUUID(), AFTER_STEP,
                    new HookDefinitionMatch(CoreHookDefinition.create(new StubHookDefinition(), UUID::randomUUID)))),
            definitionMatch);
    }

    @Test
    void run_wraps_execute_in_test_case_started_and_finished_events() {
        // Given
        MockEventBus bus = new MockEventBus();

        // When
        createTestCase(testStepUndefined).run(bus);

        // Then
        List<Object> events = bus.events.stream()
                .filter(event -> event instanceof TestCaseStarted ||
                        event instanceof TestStepFinished ||
                        event instanceof TestCaseFinished)
                .collect(Collectors.toList());
        assertEquals(5, events.size());
        assertEquals(TestCaseStarted.class, events.get(0).getClass());
        assertEquals(TestStepFinished.class, events.get(1).getClass()); // before
                                                                        // hook
        assertEquals(TestStepFinished.class, events.get(2).getClass()); // undefined
                                                                        // step
        assertEquals(TestStepFinished.class, events.get(3).getClass()); // after
                                                                        // hook
        assertEquals(TestCaseFinished.class, events.get(4).getClass());
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
    void run_all_steps() {
        // Given
        MockEventBus bus = new MockEventBus();
        TestCase testCase = createTestCase(testStep1, testStep2);

        // When
        testCase.run(bus);

        // Then
        List<TestStepStarted> testStepsStarted = bus.events.stream()
                .filter(event -> event instanceof TestStepStarted)
                .map(event -> (TestStepStarted) event)
                .filter(event -> event.getTestStep() instanceof io.cucumber.plugin.event.PickleStepTestStep)
                .collect(Collectors.toList());
        assertEquals(2, testStepsStarted.size());
        // test steps are run in order
        assertEquals(testStep1.getId(), testStepsStarted.get(0).getTestStep().getId());
        assertEquals(testStep2.getId(), testStepsStarted.get(1).getTestStep().getId());
    }

    @Test
    void run_hooks_after_the_first_non_passed_result_for_gherkin_step() {
        // Given
        MockEventBus bus = new MockEventBus();
        TestCase testCase = createTestCase(testStepUndefined, testStep2);

        // When
        testCase.run(bus);

        // Then
        List<TestStepFinished> testStepsFinished = bus.events.stream()
                .filter(event -> event instanceof TestStepFinished)
                .map(event -> (TestStepFinished) event)
                .collect(Collectors.toList());
        assertEquals(testCase.getTestSteps().size(), testStepsFinished.size());
        // run_hooks_after_the_first_non_passed_result_for_gherkin_step
        assertEquals(Status.PASSED, testStepsFinished.get(0).getResult().getStatus()); // before
                                                                                       // step1
                                                                                       // hook
        assertEquals(Status.UNDEFINED, testStepsFinished.get(1).getResult().getStatus()); // step
                                                                                          // 1
                                                                                          // (undefined)
        assertEquals(Status.PASSED, testStepsFinished.get(2).getResult().getStatus()); // after
                                                                                       // step1
                                                                                       // hook
        // skip_hooks_of_step_after_skipped_step
        assertEquals(Status.SKIPPED, testStepsFinished.get(3).getResult().getStatus()); // before
                                                                                        // step2
                                                                                        // hook
        assertEquals(Status.SKIPPED, testStepsFinished.get(4).getResult().getStatus()); // step
                                                                                        // 2
        assertEquals(Status.SKIPPED, testStepsFinished.get(5).getResult().getStatus()); // after
                                                                                        // step2
                                                                                        // hook
    }

    private static class MockEventBus implements EventBus {
        List<Object> events = new ArrayList<>();

        @Override
        public Instant getInstant() {
            return Instant.now();
        }

        @Override
        public UUID generateId() {
            return UUID.randomUUID();
        }

        @Override
        public <T> void send(T event) {
            events.add(event);
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

    private static class StubStepDefinition implements StepDefinition {
        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return null;
        }

        @Override
        public void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException {

        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return null;
        }

        @Override
        public String getPattern() {
            return null;
        }
    }

    private static class StubHookDefinition implements HookDefinition {

        private final int order;

        StubHookDefinition() {
            this(0);
        }

        StubHookDefinition(int order) {
            this.order = order;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked hook definition";
        }

        @Override
        public void execute(io.cucumber.core.backend.TestCaseState state) {

        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return order;
        }

    }
}
