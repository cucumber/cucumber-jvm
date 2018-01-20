package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.TestStepFinished;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        Result result = mock(Result.class);
        when(result.is(Result.Type.PASSED)).thenReturn(false);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0l, mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        TestStep testStep = mockTestStep();
        Result result = mock(Result.class);
        when(result.getDuration()).thenReturn(12345L);
        when(result.is(Result.Type.PASSED)).thenReturn(true);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0l, testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).name, "step");
        assertEquals(durationEntries.get(0).durations.size(), 1);
        assertEquals(durationEntries.get(0).durations.get(0).duration, BigDecimal.valueOf(12345));
    }

    @Test
    public void resultWithZeroDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        TestStep testStep = mockTestStep();
        Result result = mock(Result.class);
        when(result.getDuration()).thenReturn(0L);
        when(result.is(Result.Type.PASSED)).thenReturn(true);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0l, testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).name, "step");
        assertEquals(durationEntries.get(0).durations.size(), 1);
        assertEquals(durationEntries.get(0).durations.get(0).duration, BigDecimal.ZERO);
    }

    @Test
    public void resultWithNullDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        TestStep testStep = mockTestStep();
        Result result = mock(Result.class);
        when(result.getDuration()).thenReturn(null);
        when(result.is(Result.Type.PASSED)).thenReturn(true);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0l, testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).name, "step");
        assertEquals(durationEntries.get(0).durations.size(), 1);
        assertEquals(durationEntries.get(0).durations.get(0).duration, BigDecimal.ZERO);
    }

    @Test
    public void doneWithoutUsageStatisticStrategies() throws IOException {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer();
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration();
        stepDuration.duration = BigDecimal.valueOf(12345678L);
        stepDuration.location = "location.feature";
        stepContainer.durations = Arrays.asList(stepDuration);

        usageFormatter.usageMap.put("aStep", Arrays.asList(stepContainer));

        usageFormatter.finishReport();

        assertTrue(out.toString().contains("0.012345678"));
    }

    @Test
    public void doneWithUsageStatisticStrategies() throws IOException {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer();
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration();
        stepDuration.duration = BigDecimal.valueOf(12345678L);
        stepDuration.location = "location.feature";
        stepContainer.durations = Arrays.asList(stepDuration);

        usageFormatter.usageMap.put("aStep", Arrays.asList(stepContainer));

        UsageFormatter.UsageStatisticStrategy usageStatisticStrategy = mock(UsageFormatter.UsageStatisticStrategy.class);
        when(usageStatisticStrategy.calculate(Arrays.asList(12345678L))).thenReturn(23456L);
        usageFormatter.addUsageStatisticStrategy("average", usageStatisticStrategy);

        usageFormatter.finishReport();;

        assertTrue(out.toString().contains("0.000023456"));
        assertTrue(out.toString().contains("0.012345678"));
    }

    private TestStep mockTestStep() {
        TestStep testStep = mock(TestStep.class, Mockito.RETURNS_MOCKS);
        when(testStep.getPattern()).thenReturn("stepDef");
        when(testStep.getStepText()).thenReturn("step");
        return testStep;
    }
}
