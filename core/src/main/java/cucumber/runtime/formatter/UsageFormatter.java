package cucumber.runtime.formatter;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Plugin;
import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.NiceAppendable;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Formatter to measure performance of steps. Includes average and median step duration.
 */
final class UsageFormatter implements Plugin, EventListener {
    private static final BigDecimal NANOS_PER_SECOND = BigDecimal.valueOf(1000000000);
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

    private Map<String, BigDecimal> createAggregatedDurations(StepContainer stepContainer) {
        Map<String, BigDecimal> aggregatedResults = new LinkedHashMap<>();
        List<BigDecimal> rawDurations = getRawDurations(stepContainer.getDurations());

        BigDecimal average = calculateAverage(rawDurations);
        aggregatedResults.put("average", average);

        BigDecimal median = calculateMedian(rawDurations);
        aggregatedResults.put("median", median);

        return aggregatedResults;
    }

    private BigDecimal toSeconds(Long nanoSeconds) {
        return BigDecimal.valueOf(nanoSeconds).divide(NANOS_PER_SECOND);
    }

    private List<BigDecimal> getRawDurations(List<StepDuration> stepDurations) {
        List<BigDecimal> rawDurations = new ArrayList<>();

        for (StepDuration stepDuration : stepDurations) {
            rawDurations.add(stepDuration.duration);
        }
        return rawDurations;
    }

    private Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private void addUsageEntry(Result result, PickleStepTestStep testStep) {
        List<StepContainer> stepContainers = findOrCreateUsageEntry(testStep.getPattern());
        StepContainer stepContainer = findOrCreateStepContainer(testStep.getStepText(), stepContainers);
        StepDuration stepDuration = createStepDuration(result.getDuration(), testStep.getStepLocation());
        stepContainer.getDurations().add(stepDuration);
    }

    private List<StepContainer> findOrCreateUsageEntry(String stepDefinition) {
        List<StepContainer> stepContainers = usageMap.get(stepDefinition);
        if (stepContainers == null) {
            stepContainers = new ArrayList<>();
            usageMap.put(stepDefinition, stepContainers);
        }
        return stepContainers;
    }

    private StepDuration createStepDuration(Long duration, String location) {
        StepDuration stepDuration = new StepDuration(toSeconds(duration), location);
        return  stepDuration;
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
        private final Map<String, BigDecimal> aggregatedDurations = new HashMap<>();
        private final List<StepDuration> durations = new ArrayList<>();

        StepContainer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void putAllAggregatedDurations(Map<String, BigDecimal> aggregatedDurations) {
            this.aggregatedDurations.putAll(aggregatedDurations);
        }

        public Map<String, BigDecimal> getAggregatedDurations() {
            return aggregatedDurations;
        }

        List<StepDuration> getDurations() {
            return durations;
        }

    }

    static class StepDuration {
        private final BigDecimal duration;
        private final String location;

        StepDuration(BigDecimal duration, String location) {
            this.duration = duration;
            this.location = location;
        }

        public BigDecimal getDuration() {
            return duration;
        }

        public String getLocation() {
            return location;
        }
    }

    /**
     * Calculate the average of a list of duration entries
     */
    BigDecimal calculateAverage(List<BigDecimal> durationEntries) {
         if (durationEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.valueOf(0L);
        for (BigDecimal duration : durationEntries) {
            sum = sum.add(duration);
        }
        return sum.divide(BigDecimal.valueOf(durationEntries.size()), 9, RoundingMode.HALF_UP);
    }

    /**
     * Calculate the median of a list of duration entries
     */
    BigDecimal calculateMedian(List<BigDecimal> durationEntries) {
         if (durationEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Collections.sort(durationEntries);
        int middle = durationEntries.size() / 2;
        if (durationEntries.size() % 2 == 1) {
        return durationEntries.get(middle);
        } else {
            BigDecimal total = durationEntries.get(middle - 1).add(durationEntries.get(middle));
            return total.divide(BigDecimal.valueOf(2.0), 9, RoundingMode.HALF_UP);
        }
    }
}
