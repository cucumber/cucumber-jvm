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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class UsageFormatterTest {
    @Test
    public void close() throws IOException {
        Appendable out = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class));
        UsageFormatter usageFormatter = new UsageFormatter(out);
        usageFormatter.finishReport();
        verify((Closeable) out).close();
    }

    @Test
    public void resultWithFailedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Result.Type.FAILED, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithSkippedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Result.Type.SKIPPED, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithPendingStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Result.Type.PENDING, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithAmbiguousStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Result.Type.AMBIGUOUS, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithUndefinedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Result.Type.AMBIGUOUS, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithPassedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, 12345L, null);


        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), new BigDecimal("0.000012345"));
    }

    @Test
    public void resultWithPassedAndFailedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();

        Result passed = new Result(Result.Type.PASSED, 12345L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), testStep, passed));

        Result failed = new Result(Result.Type.FAILED, 0L, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), testStep, failed));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), new BigDecimal("0.000012345"));
    }

    @Test
    public void resultWithZeroDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, 0L, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), BigDecimal.ZERO);
    }

    // Note: Duplicate of above test
    @Test
    public void resultWithNullDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        PickleStepTestStep testStep = mockTestStep();
        Result result = new Result(Result.Type.PASSED, 0L, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(0L, 0L, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), BigDecimal.ZERO);
    }

    @Test
    public void doneWithoutUsageStatisticStrategies() {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer("a step");
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration(new BigDecimal("0.012345678"), "location.feature");
        stepContainer.getDurations().addAll(asList(stepDuration));
        usageFormatter.usageMap.put("a (.*)", asList(stepContainer));

        usageFormatter.finishReport();

        String json = "" +
            "[\n" +
            "  {\n" +
            "    \"source\": \"a (.*)\",\n" +
            "    \"steps\": [\n" +
            "      {\n" +
            "        \"name\": \"a step\",\n" +
            "        \"aggregatedDurations\": {\n" +
            "          \"median\": 0.012345678,\n" +
            "          \"average\": 0.012345678\n" +
            "        },\n" +
            "        \"durations\": [\n" +
            "          {\n" +
            "            \"duration\": 0.012345678,\n" +
            "            \"location\": \"location.feature\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";

        assertThat(out.toString(), sameJSONAs(json));
    }

    @Test
    public void doneWithUsageStatisticStrategies() {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer("a step");
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration(new BigDecimal("0.012345678"), "location.feature");
        stepContainer.getDurations().addAll(asList(stepDuration));

        usageFormatter.usageMap.put("a (.*)", asList(stepContainer));

        usageFormatter.finishReport();

        assertThat(out.toString(), containsString("0.012345678"));
        String json =
            "[\n" +
                "  {\n" +
                "    \"source\": \"a (.*)\",\n" +
                "    \"steps\": [\n" +
                "      {\n" +
                "        \"name\": \"a step\",\n" +
                "        \"aggregatedDurations\": {\n" +
                "          \"median\": 0.012345678,\n" +
                "          \"average\": 0.012345678\n" +
                "        },\n" +
                "        \"durations\": [\n" +
                "          {\n" +
                "            \"duration\": 0.012345678,\n" +
                "            \"location\": \"location.feature\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        assertThat(out.toString(), sameJSONAs(json));
    }

    @Test
    public void calculateAverageFromList() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateAverage(Arrays.asList(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L), BigDecimal.valueOf(3L)));
        assertEquals(result.toString(), "2.000000000");
    }

    @Test
    public void calculateAverageOf() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateAverage(Arrays.asList(BigDecimal.valueOf(1L), BigDecimal.valueOf(1L), BigDecimal.valueOf(2L)));
        assertEquals(result, BigDecimal.valueOf(1.333333333));
    }

    @Test
    public void calculateAverageOfEmptylist() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateAverage(Collections.EMPTY_LIST);
        assertEquals(result, BigDecimal.ZERO);
    }

    @Test
    public void calculateMedianOfOddNumberOfEntries() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateMedian(Arrays.asList(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L), BigDecimal.valueOf(3L)));
        assertEquals(result, BigDecimal.valueOf(2L));
    }

    @Test
    public void calculateMedianOfEvenNumberOfEntries() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateMedian(Arrays.asList(BigDecimal.valueOf(1L), BigDecimal.valueOf(3L), BigDecimal.valueOf(10L), BigDecimal.valueOf(5L)));
        assertEquals(result.toString(), "4.000000000");
    }

    @Test
    public void calculateMedianOf() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateMedian(Arrays.asList(BigDecimal.valueOf(2L), BigDecimal.valueOf(9L)));
        assertEquals(result.toString(), "5.500000000");
    }

    @Test
    public void calculateMedianOfEmptylist() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        BigDecimal result = usageFormatter.calculateMedian(Collections.EMPTY_LIST);
        assertEquals(result, BigDecimal.ZERO);
    }

    private PickleStepTestStep mockTestStep() {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class, Mockito.RETURNS_MOCKS);
        when(testStep.getPattern()).thenReturn("stepDef");
        when(testStep.getStepText()).thenReturn("step");
        return testStep;
    }
}
