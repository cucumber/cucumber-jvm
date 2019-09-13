package io.cucumber.core.plugin;

import java.time.Clock;
import java.time.Duration;

import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.StepDefinition;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnusedStepsSummaryPrinterTest {

    @Test
    void verifyUnusedStepsPrinted() {
        StringBuilder out = new StringBuilder();
        UnusedStepsSummaryPrinter summaryPrinter = new UnusedStepsSummaryPrinter(out);
        TimeServiceEventBus bus = new TimeServiceEventBus(Clock.systemUTC());
        summaryPrinter.setEventPublisher(bus);

        // Register two steps, use one, then finish the test run
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/belly.feature:3", "a few cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/tummy.feature:5", "some more cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/gut.feature:7", "even more cukes")));
        bus.send(new TestStepFinished(bus.getInstant(), mock(TestCase.class), mockTestStep("my/belly.feature:3"), new Result(Status.UNUSED, Duration.ZERO, null)));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/belly.feature:3", "a few cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/tummy.feature:5", "some more cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/gut.feature:7", "even more cukes")));
        bus.send(new TestStepFinished(bus.getInstant(), mock(TestCase.class), mockTestStep("my/gut.feature:7"), new Result(Status.UNUSED, Duration.ZERO, null)));
        bus.send(new TestRunFinished(bus.getInstant()));

        // Verify produced output
        assertThat(out.toString(), is(equalTo("1 Unused steps:\n" + "my/tummy.feature:5 # some more cukes\n")));
    }

    private static StepDefinition mockStepDef(String location, String pattern) {
        StepDefinition stepDef1 = mock(StepDefinition.class);
        when(stepDef1.getLocation()).thenReturn(location);
        when(stepDef1.getPattern()).thenReturn(pattern);
        return stepDef1;
    }

    private static TestStep mockTestStep(String location) {
        TestStep testStep = mock(TestStep.class);
        when(testStep.getCodeLocation()).thenReturn(location);
        return testStep;
    }

}
