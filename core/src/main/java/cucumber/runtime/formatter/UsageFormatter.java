package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Formatter to measure performance of steps. Aggregated results for all steps can be computed
 * by adding {@link UsageStatisticStrategy} to the usageFormatter
 */
final class UsageFormatter implements Formatter {
    private static final BigDecimal NANOS_PER_SECOND = BigDecimal.valueOf(1000000000);
    final Map<String, List<StepContainer>> usageMap = Collections.synchronizedMap(new HashMap<String, List<StepContainer>>());
    private final Map<String, UsageStatisticStrategy> statisticStrategies = new LinkedHashMap<String, UsageStatisticStrategy>();
    private final Object syncObject = new Object();
    private final Map<String, Object> stepContainerSyncObjects = new ConcurrentHashMap<String, Object>();

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

        addUsageStatisticStrategy("median", new MedianUsageStatisticStrategy());
        addUsageStatisticStrategy("average", new AverageUsageStatisticStrategy());
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    void handleTestStepFinished(TestStepFinished event) {
        if (!event.testStep.isHook() && event.result.is(Result.Type.PASSED)) {
            addUsageEntry(event.result, event.testStep.getPattern(), event.testStep.getStepText(), event.testStep.getStepLocation());
        }
    }

    void finishReport() {
        List<StepDefContainer> stepDefContainers = new ArrayList<StepDefContainer>();
        //Sort the output so can guarantee order of results when run in parallel
        final Map<String, List<StepContainer>> sortedMap = new TreeMap<String, List<StepContainer>>(usageMap);
        for (Map.Entry<String, List<StepContainer>> usageEntry : sortedMap.entrySet()) {
            StepDefContainer stepDefContainer = new StepDefContainer();
            stepDefContainers.add(stepDefContainer);

            stepDefContainer.source = usageEntry.getKey();
            stepDefContainer.steps = createStepContainer(usageEntry.getValue());
        }

        out.append(gson().toJson(stepDefContainers));
        out.close();
    }

    private List<StepContainer> createStepContainer(List<StepContainer> stepContainers) {
        for (StepContainer stepContainer : stepContainers) {
            stepContainer.aggregatedDurations = createAggregatedDurations(stepContainer);
            formatDurationAsSeconds(stepContainer.durations);
        }
        return stepContainers;
    }

    private void formatDurationAsSeconds(List<StepDuration> durations) {
        for (StepDuration duration : durations) {
            duration.duration = toSeconds(duration.duration.longValue());
        }
    }

    private Map<String, BigDecimal> createAggregatedDurations(StepContainer stepContainer) {
        Map<String, BigDecimal> aggregatedResults = new LinkedHashMap<String, BigDecimal>();
        for (Map.Entry<String, UsageStatisticStrategy> calculatorEntry : statisticStrategies.entrySet()) {
            UsageStatisticStrategy statisticStrategy = calculatorEntry.getValue();
            List<Long> rawDurations = getRawDurations(stepContainer.durations);
            Long calculationResult = statisticStrategy.calculate(rawDurations);

            String strategy = calculatorEntry.getKey();
            aggregatedResults.put(strategy, toSeconds(calculationResult));
        }
        return aggregatedResults;
    }

    private BigDecimal toSeconds(Long nanoSeconds) {
        return BigDecimal.valueOf(nanoSeconds).divide(NANOS_PER_SECOND);
    }

    private List<Long> getRawDurations(List<StepDuration> stepDurations) {
        List<Long> rawDurations = new ArrayList<Long>();

        for (StepDuration stepDuration : stepDurations) {
            rawDurations.add(stepDuration.duration.longValue());
        }
        return rawDurations;
    }

    private Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private void addUsageEntry(Result result, String stepDefinition, String stepNameWithArgs, String stepLocation) {
        List<StepContainer> stepContainers = usageMap.get(stepDefinition);
        if (stepContainers == null) {
            synchronized (syncObject) {
                stepContainers = usageMap.get(stepDefinition);
                if (stepContainers == null) {
                    stepContainers = new ArrayList<StepContainer>();
                    usageMap.put(stepDefinition, stepContainers);
                    stepContainerSyncObjects.put(stepDefinition, new Object());
                }
            }
        }
        StepContainer stepContainer = findOrCreateStepContainer(stepNameWithArgs, stepContainers, stepDefinition);

        Long duration = result.getDuration();
        StepDuration stepDuration = createStepDuration(duration, stepLocation);
        stepContainer.durations.add(stepDuration);
    }

    private StepDuration createStepDuration(Long duration, String location) {
        StepDuration stepDuration = new StepDuration();
        if (duration == null) {
            stepDuration.duration = BigDecimal.ZERO;
        } else {
            stepDuration.duration = BigDecimal.valueOf(duration);
        }
        stepDuration.location = location;
        return stepDuration;
    }

    private StepContainer findOrCreateStepContainer(String stepNameWithArgs, List<StepContainer> stepContainers, String stepDefinition) {
        synchronized (stepContainerSyncObjects.get(stepDefinition)) {
            for (StepContainer container : stepContainers) {
                if (stepNameWithArgs.equals(container.name)) {
                    return container;
                }
            }
            StepContainer stepContainer = new StepContainer();
            stepContainer.name = stepNameWithArgs;
            stepContainers.add(stepContainer);
            return stepContainer;
        }
    }

    /**
     * Add a {@link UsageStatisticStrategy} to the formatter
     *
     * @param key      the key, will be displayed in the output
     * @param strategy the strategy
     */
    public void addUsageStatisticStrategy(String key, UsageStatisticStrategy strategy) {
        statisticStrategies.put(key, strategy);
    }

    /**
     * Container of Step Definitions (patterns)
     */
    static class StepDefContainer {
        /**
         * The StepDefinition (pattern)
         */
        public String source;

        /**
         * A list of Steps
         */
        public List<StepContainer> steps;
    }

    /**
     * Contains for usage-entries of steps
     */
    static class StepContainer {
        public String name;
        public Map<String, BigDecimal> aggregatedDurations = new HashMap<String, BigDecimal>();
        public List<StepDuration> durations = new ArrayList<StepDuration>();
    }

    static class StepDuration {
        public BigDecimal duration;
        public String location;
    }

    /**
     * Calculate a statistical value to be displayed in the usage-file
     */
    interface UsageStatisticStrategy {
        /**
         * @param durationEntries list of execution times of steps as nanoseconds
         * @return a statistical value (e.g. median, average, ..)
         */
        Long calculate(List<Long> durationEntries);
    }

    /**
     * Calculate the average of a list of duration entries
     */
    static class AverageUsageStatisticStrategy implements UsageStatisticStrategy {
        @Override
        public Long calculate(List<Long> durationEntries) {
            if (verifyNoNulls(durationEntries)) {
                return 0L;
            }

            long sum = 0;
            for (Long duration : durationEntries) {
                sum += duration;
            }
            return sum / durationEntries.size();
        }

        private boolean verifyNoNulls(List<Long> durationEntries) {
            return durationEntries == null || durationEntries.isEmpty() || durationEntries.contains(null);
        }
    }

    /**
     * Calculate the median of a list of duration entries
     */
    static class MedianUsageStatisticStrategy implements UsageStatisticStrategy {
        @Override
        public Long calculate(List<Long> durationEntries) {
            if (verifyNoNulls(durationEntries)) {
                return 0L;
            }
            Collections.sort(durationEntries);
            int middle = durationEntries.size() / 2;
            if (durationEntries.size() % 2 == 1) {
                return durationEntries.get(middle);
            } else {
                return (durationEntries.get(middle - 1) + durationEntries.get(middle)) / 2;
            }
        }

        private boolean verifyNoNulls(List<Long> durationEntries) {
            return durationEntries == null || durationEntries.isEmpty() || durationEntries.contains(null);
        }
    }
}
