package io.cucumber.core.plugin;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.JsonPrimitive;
import gherkin.deps.com.google.gson.JsonSerializer;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.plugin.Plugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Formatter to measure performance of steps. Includes average and median step duration.
 */
public final class UsageFormatter implements Plugin, EventListener {

    private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
    final Map<String, List<StepContainer>> usageMap = new LinkedHashMap<>();
    private final NiceAppendable out;

    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

    /**
     * Constructor
     *
     * @param out {@link Appendable} to print the result
     */
    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public UsageFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    void handleTestStepFinished(TestStepFinished event) {
        if (event.testStep instanceof PickleStepTestStep && event.result.is(Result.Type.PASSED)) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
            addUsageEntry(event.result, testStep);
        }
    }

    void finishReport() {
        List<StepDefContainer> stepDefContainers = new ArrayList<>();
        for (Map.Entry<String, List<StepContainer>> usageEntry : usageMap.entrySet()) {
            StepDefContainer stepDefContainer = new StepDefContainer(
                usageEntry.getKey(),
                createStepContainers(usageEntry.getValue())
            );
            stepDefContainers.add(stepDefContainer);
        }

        out.append(gson().toJson(stepDefContainers));
        out.close();
    }

    private List<StepContainer> createStepContainers(List<StepContainer> stepContainers) {
        for (StepContainer stepContainer : stepContainers) {
            stepContainer.putAllAggregatedDurations(createAggregatedDurations(stepContainer));
        }
        return stepContainers;
    }

    private Map<String, Duration> createAggregatedDurations(StepContainer stepContainer) {
        Map<String, Duration> aggregatedResults = new LinkedHashMap<>();
        List<Duration> rawDurations = getRawDurations(stepContainer.getDurations());

        Duration average = calculateAverage(rawDurations);
        aggregatedResults.put("average", average);

        Duration median = calculateMedian(rawDurations);
        aggregatedResults.put("median", median);

        return aggregatedResults;
    }

    private List<Duration> getRawDurations(List<StepDuration> stepDurations) {
        List<Duration> rawDurations = new ArrayList<>();

        for (StepDuration stepDuration : stepDurations) {
            rawDurations.add(stepDuration.duration);
        }
        return rawDurations;
    }

    private Gson gson() {
        JsonSerializer<Duration> durationJsonSerializer = (duration, returnVal, jsonSerializationContext) ->
            new JsonPrimitive((double) duration.getNano() / NANOS_PER_SECOND);

        return new GsonBuilder()
            .registerTypeAdapter(Duration.class, durationJsonSerializer)
            .setPrettyPrinting()
            .create();
    }

    private void addUsageEntry(Result result, PickleStepTestStep testStep) {
        List<StepContainer> stepContainers = usageMap.computeIfAbsent(testStep.getPattern(), k -> new ArrayList<>());
        StepContainer stepContainer = findOrCreateStepContainer(testStep.getStepText(), stepContainers);
        StepDuration stepDuration = new StepDuration(result.getDuration(), testStep.getStepLocation());
        stepContainer.getDurations().add(stepDuration);
    }

    private StepContainer findOrCreateStepContainer(String stepNameWithArgs, List<StepContainer> stepContainers) {
        for (StepContainer container : stepContainers) {
            if (stepNameWithArgs.equals(container.getName())) {
                return container;
            }
        }
        StepContainer stepContainer = new StepContainer(stepNameWithArgs);
        stepContainers.add(stepContainer);
        return stepContainer;
    }

    /**
     * Calculate the average of a list of duration entries
     */
    Duration calculateAverage(List<Duration> durationEntries) {

        Duration sum = Duration.ZERO;
        for (Duration duration : durationEntries) {
            sum = sum.plus(duration);
        }
        if (sum.isZero()) {
            return Duration.ZERO;
        }

        return sum.dividedBy(durationEntries.size());
    }

    /**
     * Calculate the median of a list of duration entries
     */
    Duration calculateMedian(List<Duration> durationEntries) {
        if (durationEntries.isEmpty()) {
            return Duration.ZERO;
        }
        Collections.sort(durationEntries);
        int middle = durationEntries.size() / 2;
        if (durationEntries.size() % 2 == 1) {
            return durationEntries.get(middle);
        } else {
            Duration total = durationEntries.get(middle - 1).plus(durationEntries.get(middle));
            return total.dividedBy(2);
        }
    }

    /**
     * Container of Step Definitions (patterns)
     */
    static class StepDefContainer {
        private final String source;
        private final List<StepContainer> steps;

        StepDefContainer(String source, List<StepContainer> steps) {
            this.source = source;
            this.steps = steps;
        }

        /**
         * The StepDefinition (pattern)
         */
        public String getSource() {
            return source;
        }

        /**
         * A list of Steps
         */
        public List<StepContainer> getSteps() {
            return steps;
        }

    }

    /**
     * Container for usage-entries of steps
     */
    static class StepContainer {
        private final String name;
        private final Map<String, Duration> aggregatedDurations = new HashMap<>();
        private final List<StepDuration> durations = new ArrayList<>();

        StepContainer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void putAllAggregatedDurations(Map<String, Duration> aggregatedDurations) {
            this.aggregatedDurations.putAll(aggregatedDurations);
        }

        public Map<String, Duration> getAggregatedDurations() {
            return aggregatedDurations;
        }

        List<StepDuration> getDurations() {
            return durations;
        }

    }

    static class StepDuration {
        private final Duration duration;
        private final String location;

        StepDuration(Duration duration, String location) {
            this.duration = duration;
            this.location = location;
        }

        public Duration getDuration() {
            return duration;
        }

        public String getLocation() {
            return location;
        }
    }
}
