package cucumber.runner;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCaseTest {
    private final static String ENGLISH = "en";

    @Test
    public void run_wraps_execute_in_test_case_started_and_finished_events() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        when(testStep.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.UNDEFINED));

        TestCase testCase = createTestCase(testStep);
        testCase.run(bus);

        InOrder order = inOrder(bus, testStep);
        order.verify(bus).send(isA(TestCaseStarted.class));
        order.verify(testStep).run(eq(testCase), eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(bus).send(isA(TestCaseFinished.class));
    }

    @Test
    public void run_all_steps() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        PickleStepTestStep testStep1 = mock(PickleStepTestStep.class);
        when(testStep1.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.PASSED));
        PickleStepTestStep testStep2 = mock(PickleStepTestStep.class);
        when(testStep2.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.PASSED));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), eq(false));
    }

    @Test
    public void run_hooks_after_the_first_non_passed_result_for_gherkin_step() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        PickleStepTestStep testStep1 = mock(PickleStepTestStep.class);
        when(testStep1.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.UNDEFINED));
        PickleStepTestStep testStep2 = mock(PickleStepTestStep.class);
        when(testStep2.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.SKIPPED));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(eq(testCase), eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(eq(testCase), eq(bus), eq(language), isA(Scenario.class), eq(true));
    }

    @Test
    public void skip_steps_at_first_gherkin_step_after_nonn_passed_result() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        PickleStepTestStep testStep1 = mock(PickleStepTestStep.class);
        when(testStep1.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.UNDEFINED));
        PickleStepTestStep testStep2 = mock(PickleStepTestStep.class);
        when(testStep2.run(any(TestCase.class), eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.SKIPPED));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(eq(testCase), eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(eq(testCase), eq(bus), eq(language), isA(Scenario.class), eq(true));
    }

    private TestCase createTestCase(PickleStepTestStep... steps) {
        return new TestCase(Arrays.asList(steps), Collections.<HookTestStep>emptyList(), Collections.<HookTestStep>emptyList(), pickleEvent(), false);
    }

    private PickleEvent pickleEvent() {
        Pickle pickle = mock(Pickle.class);
        when(pickle.getLanguage()).thenReturn(ENGLISH);
        when(pickle.getLocations()).thenReturn(Arrays.asList(new PickleLocation(1, 1)));
        return new PickleEvent("uri", pickle);
    }

    private Result resultWithStatus(Result.Type status) {
        return new Result(status, null, null);
    }
}
