package io.cucumber.core.plugin;

import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestStep;
import io.cucumber.core.event.TestStepFinished;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
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
        Result result = new Result(Status.FAILED, Duration.ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithSkippedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Status.SKIPPED, Duration.ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithPendingStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Status.PENDING, Duration.ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithAmbiguousStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Status.AMBIGUOUS, Duration.ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithUndefinedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = new Result(Status.AMBIGUOUS, Duration.ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), mockTestStep(), result));
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithPassedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();
        Result result = new Result(Status.PASSED, Duration.ofNanos(12345L), null);


        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), Duration.ofNanos(12345L));
    }

    @Test
    public void resultWithPassedAndFailedStep() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();

        Result passed = new Result(Status.PASSED, Duration.ofSeconds(12345L), null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, passed));

        Result failed = new Result(Status.FAILED, Duration.ZERO, null);
        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, failed));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), Duration.ofSeconds(12345));
    }

    @Test
    public void resultWithZeroDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();
        Result result = new Result(Status.PASSED, Duration.ZERO, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), Duration.ZERO);
    }

    // Note: Duplicate of above test
    @Test
    public void resultWithNullDuration() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        PickleStepTestStep testStep = mockTestStep();
        Result result = new Result(Status.PASSED, Duration.ZERO, null);

        usageFormatter.handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0).getName(), "step");
        assertEquals(durationEntries.get(0).getDurations().size(), 1);
        assertEquals(durationEntries.get(0).getDurations().get(0).getDuration(), Duration.ZERO);
    }

    @Test
    public void doneWithoutUsageStatisticStrategies() {
        StringBuffer out = new StringBuffer();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer("a step");
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration(Duration.ofNanos(12345678L), "location.feature");
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
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration(Duration.ofNanos(12345678L), "location.feature");
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
        Duration result = usageFormatter.calculateAverage(asList(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(3L)));
        assertEquals(Duration.ofSeconds(2L), result);
    }

    @Test
    public void calculateAverageOf() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateAverage(asList(Duration.ofSeconds(1L), Duration.ofSeconds(1L), Duration.ofSeconds(2L)));
        assertEquals(Duration.ofNanos(1333333333), result);
    }

    @Test
    public void calculateAverageOfEmptylist() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateAverage(Collections.emptyList());
        assertEquals(Duration.ZERO, result);
    }

    @Test
    public void calculateMedianOfOddNumberOfEntries() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(asList(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(3L)));
        assertEquals(Duration.ofSeconds(2L), result);
    }

    @Test
    public void calculateMedianOfEvenNumberOfEntries() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(asList(Duration.ofSeconds(1L), Duration.ofSeconds(3L), Duration.ofSeconds(10L), Duration.ofSeconds(5L)));
        assertEquals(Duration.ofSeconds(4), result);
    }

    @Test
    public void calculateMedianOf() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(asList(Duration.ofSeconds(2L), Duration.ofSeconds(9L)));
        assertEquals(Duration.ofMillis(5500), result);
    }

    @Test
    public void calculateMedianOfEmptylist() {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(Collections.emptyList());
        assertEquals(Duration.ZERO, result);
    }

    private PickleStepTestStep mockTestStep() {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class, Mockito.RETURNS_MOCKS);
        when(testStep.getPattern()).thenReturn("stepDef");
        when(testStep.getStepText()).thenReturn("step");
        return testStep;
    }

}
