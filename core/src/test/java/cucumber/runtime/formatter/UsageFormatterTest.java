package cucumber.runtime.formatter;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.TestStepFinished;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
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
        Result result = new Result(Result.Type.FAILED, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        TestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, 12345L, null);


        usageFormatter.handleTestStepFinished(new TestStepFinished(0l, mock(TestCase.class), testStep, result));

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
        Result result = new Result(Result.Type.PASSED, 0L, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, mock(TestCase.class), testStep, result));

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

        PickleStepTestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, 0L, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0L,mock(TestCase.class), testStep, result));

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
        stepContainer.durations = asList(stepDuration);

        usageFormatter.usageMap.put("aStep", asList(stepContainer));

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
        stepContainer.durations = asList(stepDuration);

        usageFormatter.usageMap.put("aStep", asList(stepContainer));

        UsageFormatter.UsageStatisticStrategy usageStatisticStrategy = mock(UsageFormatter.UsageStatisticStrategy.class);
        when(usageStatisticStrategy.calculate(asList(12345678L))).thenReturn(23456L);
        usageFormatter.addUsageStatisticStrategy("average", usageStatisticStrategy);

        usageFormatter.finishReport();

        assertTrue(out.toString().contains("0.000023456"));
        assertTrue(out.toString().contains("0.012345678"));
    }

    private PickleStepTestStep mockTestStep() {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class, Mockito.RETURNS_MOCKS);
        when(testStep.getPattern()).thenReturn("stepDef");
        when(testStep.getStepText()).thenReturn("step");
        return testStep;
    }
}
