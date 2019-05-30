package io.cucumber.core.plugin;

import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.TestStep;
import io.cucumber.core.api.event.TestStepFinished;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Instant.EPOCH;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class UsageFormatterTest {
    @Test
    public void close() throws IOException {
        Appendable out = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class));
        UsageFormatter usageFormatter = new UsageFormatter(out);
        usageFormatter.finishReport();
        verify((Closeable) out).close();
    }

    @Test
    public void resultWithoutSkippedSteps() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Result.Type.FAILED, ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(EPOCH, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        TestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, ofMillis(12345L), null);


        usageFormatter.handleTestStepFinished(new TestStepFinished(EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).name, "step");
        assertEquals(durationEntries.get(0).durations.size(), 1);
        assertEquals(durationEntries.get(0).durations.get(0).duration, Duration.ofMillis(12345));
    }

    @Test
    public void resultWithZeroDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        TestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, ZERO, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).name, "step");
        assertEquals(durationEntries.get(0).durations.size(), 1);
        assertEquals(durationEntries.get(0).durations.get(0).duration, Duration.ZERO);
    }

    @Test
    public void resultWithNullDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        PickleStepTestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, ZERO, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).name, "step");
        assertEquals(durationEntries.get(0).durations.size(), 1);
        assertEquals(durationEntries.get(0).durations.get(0).duration, Duration.ZERO);
    }

    @Test
    public void doneWithoutUsageStatisticStrategies() {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer();
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration();
        stepDuration.duration = ofMillis(12345678L);
        stepDuration.location = "location.feature";
        stepContainer.durations = asList(stepDuration);

        usageFormatter.usageMap.put("aStep", asList(stepContainer));

        usageFormatter.finishReport();

        assertThat(out.toString(), containsString("12345"));
    }

    @Test
    public void doneWithUsageStatisticStrategies() {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer();
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration();
        stepDuration.duration = ofMillis(12345678L);
        stepDuration.location = "location.feature";
        stepContainer.durations = asList(stepDuration);

        usageFormatter.usageMap.put("aStep", asList(stepContainer));

        UsageFormatter.UsageStatisticStrategy usageStatisticStrategy = mock(UsageFormatter.UsageStatisticStrategy.class);
        when(usageStatisticStrategy.calculate(asList(ofMillis(12345678L)))).thenReturn(ofMillis(23456L));
        usageFormatter.addUsageStatisticStrategy("average", usageStatisticStrategy);

        usageFormatter.finishReport();

        assertThat(out.toString(), containsString("12345"));
        assertThat(out.toString(), containsString("12345"));
    }

    private PickleStepTestStep mockTestStep() {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class, Mockito.RETURNS_MOCKS);
        when(testStep.getPattern()).thenReturn("stepDef");
        when(testStep.getStepText()).thenReturn("step");
        return testStep;
    }
}
