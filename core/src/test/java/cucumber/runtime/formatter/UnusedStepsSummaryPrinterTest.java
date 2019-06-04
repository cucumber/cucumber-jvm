package cucumber.runtime.formatter;

import cucumber.api.TestStep;
import cucumber.api.event.StepDefinedEvent;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepFinished;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.StepDefinition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnusedStepsSummaryPrinterTest {
    @Test
    public void verifyUnusedStepsPrinted() {
        StringBuilder out = new StringBuilder();
        UnusedStepsSummaryPrinter summaryPrinter = new UnusedStepsSummaryPrinter(out);
        TimeServiceEventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);
        summaryPrinter.setEventPublisher(bus);

        // Register two steps, use one, then finish the test run
        bus.send(new StepDefinedEvent(bus.getTime(), bus.getTimeMillis(), mockStepDef("my/belly.feature:3", "a few cukes")));
        bus.send(new StepDefinedEvent(bus.getTime(), bus.getTimeMillis(), mockStepDef("my/tummy.feature:5", "some more cukes")));
        bus.send(new TestStepFinished(bus.getTime(), bus.getTimeMillis(), null, mockTestStep("my/belly.feature:3"), null));
        bus.send(new TestRunFinished(bus.getTime(), bus.getTimeMillis()));

        // Verify produced output
        assertEquals("1 Unused steps:\n" + "my/tummy.feature:5 # some more cukes\n", out.toString());
    }

    private static StepDefinition mockStepDef(String location, String pattern) {
        StepDefinition stepDef1 = mock(StepDefinition.class);
        when(stepDef1.getLocation(false)).thenReturn(location);
        when(stepDef1.getPattern()).thenReturn(pattern);
        return stepDef1;
    }

    private static TestStep mockTestStep(String location) {
        TestStep testStep = mock(TestStep.class);
        when(testStep.getCodeLocation()).thenReturn(location);
        return testStep;
    }
}
