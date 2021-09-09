package io.cucumber.core.plugin;

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

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnusedStepsSummaryPrinterTest {

    @Test
    void verifyUnusedStepsPrinted() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UnusedStepsSummaryPrinter summaryPrinter = new UnusedStepsSummaryPrinter(out);
        summaryPrinter.setMonochrome(true);
        TimeServiceEventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        summaryPrinter.setEventPublisher(bus);

        // Register two steps, use one, then finish the test run
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/belly.feature:3", "a few cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/tummy.feature:5", "some more cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/gut.feature:7", "even more cukes")));
        bus.send(new TestStepFinished(bus.getInstant(), mock(TestCase.class), mockTestStep("my/belly.feature:3"),
            new Result(Status.UNUSED, Duration.ZERO, null)));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/belly.feature:3", "a few cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/tummy.feature:5", "some more cukes")));
        bus.send(new StepDefinedEvent(bus.getInstant(), mockStepDef("my/gut.feature:7", "even more cukes")));
        bus.send(new TestStepFinished(bus.getInstant(), mock(TestCase.class), mockTestStep("my/gut.feature:7"),
            new Result(Status.UNUSED, Duration.ZERO, null)));
        bus.send(new TestRunFinished(bus.getInstant(), new Result(Status.PASSED, Duration.ZERO, null)));

        // Verify produced output
        assertThat(out, isBytesEqualTo("1 Unused steps:\n" + "my/tummy.feature:5 # some more cukes\n"));
    }

    private static StepDefinition mockStepDef(String location, String pattern) {
        return new StepDefinition(location, pattern);
    }

    private static TestStep mockTestStep(String location) {
        TestStep testStep = mock(TestStep.class);
        when(testStep.getCodeLocation()).thenReturn(location);
        return testStep;
    }

}
