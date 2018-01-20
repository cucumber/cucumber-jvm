package cucumber.api;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCaseTest {
    private final static String ENGLISH = "en";

    @Test
    public void run_wraps_execute_in_test_case_started_and_finished_events() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        TestStep testStep = mock(TestStep.class);
        when(testStep.run(eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.UNDEFINED));

        TestCase testCase = new TestCase(Arrays.asList(testStep), pickleEvent(), false);
        testCase.run(bus);

        InOrder order = inOrder(bus, testStep);
        order.verify(bus).send(isA(TestCaseStarted.class));
        order.verify(testStep).run(eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(bus).send(isA(TestCaseFinished.class));
    }

    @Test
    public void run_all_steps() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        TestStep testStep1 = mock(TestStep.class);
        when(testStep1.run(eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.PASSED));
        TestStep testStep2 = mock(TestStep.class);
        when(testStep2.run(eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.PASSED));

        TestCase testCase = new TestCase(Arrays.asList(testStep1, testStep2), pickleEvent(), false);
        testCase.run(bus);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(eq(bus), eq(language), isA(Scenario.class), eq(false));
    }

    @Test
    public void skip_steps_after_the_first_non_passed_result() throws Throwable {
        EventBus bus = mock(EventBus.class);
        String language = ENGLISH;
        TestStep testStep1 = mock(TestStep.class);
        when(testStep1.run(eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.UNDEFINED));
        TestStep testStep2 = mock(TestStep.class);
        when(testStep2.run(eq(bus), eq(language), isA(Scenario.class), anyBoolean())).thenReturn(resultWithStatus(Result.Type.SKIPPED));

        TestCase testCase = new TestCase(Arrays.asList(testStep1, testStep2), pickleEvent(), false);
        testCase.run(bus);

        InOrder order = inOrder(testStep1, testStep2);
        order.verify(testStep1).run(eq(bus), eq(language), isA(Scenario.class), eq(false));
        order.verify(testStep2).run(eq(bus), eq(language), isA(Scenario.class), eq(true));
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
