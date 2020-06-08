package io.cucumber.core.plugin;

import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

class UsageFormatterTest {

    @Test
    void resultWithPassedStep() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();
        Result result = new Result(Status.PASSED, Duration.ofNanos(12345L), null);

        usageFormatter
                .handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertThat(usageMap.size(), is(equalTo(1)));
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertThat(durationEntries.size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getName(), is(equalTo("step")));
        assertThat(durationEntries.get(0).getDurations().size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getDurations().get(0).getDuration(), is(equalTo(Duration.ofNanos(12345L))));
    }

    private PickleStepTestStep mockTestStep() {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class, Mockito.RETURNS_MOCKS);
        when(testStep.getPattern()).thenReturn("stepDef");
        when(testStep.getStepText()).thenReturn("step");
        return testStep;
    }

    @Test
    void resultWithPassedAndFailedStep() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();

        Result passed = new Result(Status.PASSED, Duration.ofSeconds(12345L), null);
        usageFormatter
                .handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, passed));

        Result failed = new Result(Status.FAILED, Duration.ZERO, null);
        usageFormatter
                .handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, failed));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertThat(usageMap.size(), is(equalTo(1)));
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertThat(durationEntries.size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getName(), is(equalTo("step")));
        assertThat(durationEntries.get(0).getDurations().size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getDurations().get(0).getDuration(), is(equalTo(Duration.ofSeconds(12345))));
    }

    @Test
    void resultWithZeroDuration() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        TestStep testStep = mockTestStep();
        Result result = new Result(Status.PASSED, Duration.ZERO, null);

        usageFormatter
                .handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertThat(usageMap.size(), is(equalTo(1)));
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertThat(durationEntries.size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getName(), is(equalTo("step")));
        assertThat(durationEntries.get(0).getDurations().size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getDurations().get(0).getDuration(), is(equalTo(Duration.ZERO)));
    }

    // Note: Duplicate of above test
    @Test
    void resultWithNullDuration() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        PickleStepTestStep testStep = mockTestStep();
        Result result = new Result(Status.PASSED, Duration.ZERO, null);

        usageFormatter
                .handleTestStepFinished(new TestStepFinished(Instant.EPOCH, mock(TestCase.class), testStep, result));

        Map<String, List<UsageFormatter.StepContainer>> usageMap = usageFormatter.usageMap;
        assertThat(usageMap.size(), is(equalTo(1)));
        List<UsageFormatter.StepContainer> durationEntries = usageMap.get("stepDef");
        assertThat(durationEntries.size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getName(), is(equalTo("step")));
        assertThat(durationEntries.get(0).getDurations().size(), is(equalTo(1)));
        assertThat(durationEntries.get(0).getDurations().get(0).getDuration(), is(equalTo(Duration.ZERO)));
    }

    @Test
    void doneWithoutUsageStatisticStrategies() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer("a step");
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration(Duration.ofNanos(1234567800L),
            "location.feature");
        stepContainer.getDurations().addAll(singletonList(stepDuration));
        usageFormatter.usageMap.put("a (.*)", singletonList(stepContainer));

        usageFormatter.finishReport();

        String json = "" +
                "[\n" +
                "  {\n" +
                "    \"source\": \"a (.*)\",\n" +
                "    \"steps\": [\n" +
                "      {\n" +
                "        \"name\": \"a step\",\n" +
                "        \"aggregatedDurations\": {\n" +
                "          \"median\": 1.2345678,\n" +
                "          \"average\": 1.2345678\n" +
                "        },\n" +
                "        \"durations\": [\n" +
                "          {\n" +
                "            \"duration\": 1.2345678,\n" +
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
    void doneWithUsageStatisticStrategies() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageFormatter.StepContainer stepContainer = new UsageFormatter.StepContainer("a step");
        UsageFormatter.StepDuration stepDuration = new UsageFormatter.StepDuration(Duration.ofNanos(12345678L),
            "location.feature");
        stepContainer.getDurations().addAll(singletonList(stepDuration));

        usageFormatter.usageMap.put("a (.*)", singletonList(stepContainer));

        usageFormatter.finishReport();

        assertThat(out.toString(), containsString("0.012345678"));
        String json = "[\n" +
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
    void calculateAverageFromList() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter
                .calculateAverage(asList(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(3L)));
        assertThat(result, is(equalTo(Duration.ofSeconds(2L))));
    }

    @Test
    void calculateAverageOf() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter
                .calculateAverage(asList(Duration.ofSeconds(1L), Duration.ofSeconds(1L), Duration.ofSeconds(2L)));
        assertThat(result, is(equalTo(Duration.ofNanos(1333333333))));
    }

    @Test
    void calculateAverageOfEmptylist() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateAverage(Collections.emptyList());
        assertThat(result, is(equalTo(Duration.ZERO)));
    }

    @Test
    void calculateMedianOfOddNumberOfEntries() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter
                .calculateMedian(asList(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(3L)));
        assertThat(result, is(equalTo(Duration.ofSeconds(2L))));
    }

    @Test
    void calculateMedianOfEvenNumberOfEntries() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(
            asList(Duration.ofSeconds(1L), Duration.ofSeconds(3L), Duration.ofSeconds(10L), Duration.ofSeconds(5L)));
        assertThat(result, is(equalTo(Duration.ofSeconds(4))));
    }

    @Test
    void calculateMedianOf() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(asList(Duration.ofSeconds(2L), Duration.ofSeconds(9L)));
        assertThat(result, is(equalTo(Duration.ofMillis(5500))));
    }

    @Test
    void calculateMedianOfEmptylist() {
        OutputStream out = new ByteArrayOutputStream();
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Duration result = usageFormatter.calculateMedian(Collections.emptyList());
        assertThat(result, is(equalTo(Duration.ZERO)));
    }

}
