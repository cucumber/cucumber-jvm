package io.cucumber.core.plugin;

import io.cucumber.messages.JSON;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStepFinished;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UsageFormatter implements Plugin, ConcurrentEventListener {

    final Map<String, List<StepContainer>> usageMap = new LinkedHashMap<>();
    private final UTF8OutputStreamWriter out;

    /**
     * Constructor
     *
     * @param out {@link Appendable} to print the result
     */
    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public UsageFormatter(OutputStream out) {
        this.out = new UTF8OutputStreamWriter(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep && event.getResult().getStatus().is(Status.PASSED)) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            addUsageEntry(event.getResult(), testStep);
        }
    }

    void finishReport() {
        List<StepDefContainer> stepDefContainers = new ArrayList<>();
        for (Map.Entry<String, List<StepContainer>> usageEntry : usageMap.entrySet()) {
            StepDefContainer stepDefContainer = new StepDefContainer(
                usageEntry.getKey(),
                createStepContainers(usageEntry.getValue()));
            stepDefContainers.add(stepDefContainer);
        }

        try {
            JSON.writeValue(out, stepDefContainers);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addUsageEntry(Result result, PickleStepTestStep testStep) {
        List<StepContainer> stepContainers = usageMap.computeIfAbsent(testStep.getPattern(), k -> new ArrayList<>());
        StepContainer stepContainer = findOrCreateStepContainer(testStep.getStepText(), stepContainers);
        StepDuration stepDuration = new StepDuration(result.getDuration(),
            testStep.getUri() + ":" + testStep.getStepLine());
        stepContainer.getDurations().add(stepDuration);
    }

    private List<StepContainer> createStepContainers(List<StepContainer> stepContainers) {
        for (StepContainer stepContainer : stepContainers) {
            stepContainer.putAllAggregatedDurations(createAggregatedDurations(stepContainer));
        }
        return stepContainers;
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

    private Map<String, Double> createAggregatedDurations(StepContainer stepContainer) {
        Map<String, Double> aggregatedResults = new LinkedHashMap<>();
        List<Double> rawDurations = getRawDurations(stepContainer.getDurations());

        Double average = calculateAverage(rawDurations);
        aggregatedResults.put("average", average);

        Double median = calculateMedian(rawDurations);
        aggregatedResults.put("median", median);

        return aggregatedResults;
    }

    private List<Double> getRawDurations(List<StepDuration> stepDurations) {
        List<Double> rawDurations = new ArrayList<>();

        for (StepDuration stepDuration : stepDurations) {
            rawDurations.add(stepDuration.duration);
        }
        return rawDurations;
    }

    /**
     * Calculate the average of a list of duration entries
     */
    Double calculateAverage(List<Double> durationEntries) {
        double sum = 0.0;
        for (Double duration : durationEntries) {
            sum = sum + duration;
        }
        if (sum == 0) {
            return 0.0;
        }

        return sum / durationEntries.size();
    }

    /**
     * Calculate the median of a list of duration entries
     */
    Double calculateMedian(List<Double> durationEntries) {
        if (durationEntries.isEmpty()) {
            return 0.0;
        }
        Collections.sort(durationEntries);
        int middle = durationEntries.size() / 2;
        if (durationEntries.size() % 2 == 1) {
            return durationEntries.get(middle);
        } else {
            double total = durationEntries.get(middle - 1) + durationEntries.get(middle);
            return total / 2;
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
        private final Map<String, Double> aggregatedDurations = new HashMap<>();
        private final List<StepDuration> durations = new ArrayList<>();

        StepContainer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void putAllAggregatedDurations(Map<String, Double> aggregatedDurations) {
            this.aggregatedDurations.putAll(aggregatedDurations);
        }

        public Map<String, Double> getAggregatedDurations() {
            return aggregatedDurations;
        }

        List<StepDuration> getDurations() {
            return durations;
        }

    }

    private static double durationToSeconds(Duration duration) {
        return (double) duration.toNanos() / TimeUnit.SECONDS.toNanos(1);
    }

    static class StepDuration {

        private final double duration;
        private final String location;

        StepDuration(Duration duration, String location) {
            this.duration = durationToSeconds(duration);
            this.location = location;
        }

        public double getDuration() {
            return duration;
        }

        public String getLocation() {
            return location;
        }

    }

}
