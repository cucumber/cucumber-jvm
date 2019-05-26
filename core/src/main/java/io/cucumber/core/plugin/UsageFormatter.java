package io.cucumber.core.plugin;

import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.plugin.Plugin;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestStepFinished;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.JsonSerializer;

import static java.time.Duration.ZERO;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formatter to measure performance of steps. Aggregated results for all steps can be computed
 * by adding {@link UsageStatisticStrategy} to the usageFormatter
 */
public final class UsageFormatter implements Plugin, EventListener {
    final Map<String, List<StepContainer>> usageMap = new HashMap<String, List<StepContainer>>();
    private final Map<String, UsageStatisticStrategy> statisticStrategies = new HashMap<String, UsageStatisticStrategy>();

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
        if (event.testStep instanceof PickleStepTestStep && event.result.is(Result.Type.PASSED)) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
            addUsageEntry(event.result, testStep.getPattern(), testStep.getStepText(), testStep.getStepLocation());
        }
    }

    void finishReport() {
        List<StepDefContainer> stepDefContainers = new ArrayList<StepDefContainer>();
        for (Map.Entry<String, List<StepContainer>> usageEntry : usageMap.entrySet()) {
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
        }
        return stepContainers;
    }

    private Map<String, Duration> createAggregatedDurations(StepContainer stepContainer) {
        Map<String, Duration> aggregatedResults = new HashMap<String, Duration>();
        for (Map.Entry<String, UsageStatisticStrategy> calculatorEntry : statisticStrategies.entrySet()) {
            UsageStatisticStrategy statisticStrategy = calculatorEntry.getValue();
            List<Duration> rawDurations = getRawDurations(stepContainer.durations);
            Duration calculationResult = statisticStrategy.calculate(rawDurations);

            String strategy = calculatorEntry.getKey();
            aggregatedResults.put(strategy, calculationResult);
        }
        return aggregatedResults;
    }

    private List<Duration> getRawDurations(List<StepDuration> stepDurations) {
        List<Duration> rawDurations = new ArrayList<Duration>();

        for (StepDuration stepDuration : stepDurations) {
            rawDurations.add(stepDuration.duration);
        }
        return rawDurations;
    }

    private Gson gson() {
        return new GsonBuilder().registerTypeAdapter(Duration.class,
                (JsonSerializer<Duration>) (duration, returnVal, jsonSerializationContext) -> {
                    return new Gson().toJsonTree(duration.getSeconds());
                }).setPrettyPrinting().create();
    }

    private void addUsageEntry(Result result, String stepDefinition, String stepNameWithArgs, String stepLocation) {
        List<StepContainer> stepContainers = usageMap.get(stepDefinition);
        if (stepContainers == null) {
            stepContainers = new ArrayList<StepContainer>();
            usageMap.put(stepDefinition, stepContainers);
        }
        StepContainer stepContainer = findOrCreateStepContainer(stepNameWithArgs, stepContainers);

        Duration duration = result.getDuration();
        StepDuration stepDuration = createStepDuration(duration, stepLocation);
        stepContainer.durations.add(stepDuration);
    }

    private StepDuration createStepDuration(Duration duration, String location) {
        StepDuration stepDuration = new StepDuration();
        if (duration == null) {
            stepDuration.duration = ZERO;
        } else {
            stepDuration.duration = duration;
        }
        stepDuration.location = location;
        return stepDuration;
    }

    private StepContainer findOrCreateStepContainer(String stepNameWithArgs, List<StepContainer> stepContainers) {
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

    /**
     * Add a {@link UsageStatisticStrategy} to the plugin
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
        public Map<String, Duration> aggregatedDurations = new HashMap<String, Duration>();
        public List<StepDuration> durations = new ArrayList<StepDuration>();
    }

    static class StepDuration {
        public Duration duration;
        public String location;
    }

    /**
     * Calculate a statistical value to be displayed in the usage-file
     */
    static interface UsageStatisticStrategy {
        /**
         * @param durationEntries list of execution times of steps as nanoseconds
         * @return a statistical value (e.g. median, average, ..)
         */
        Duration calculate(List<Duration> durationEntries);
    }

    /**
     * Calculate the average of a list of duration entries
     */
    static class AverageUsageStatisticStrategy implements UsageStatisticStrategy {
        @Override
        public Duration calculate(List<Duration> durationEntries) {
            if (verifyNoNulls(durationEntries)) {
                return ZERO;
            }

            Duration sum = ZERO;
            for (Duration duration : durationEntries) {
                sum = sum.plus(duration);
            }
            
            return sum.dividedBy(durationEntries.size());
        }

        private boolean verifyNoNulls(List<Duration> durationEntries) {
            return durationEntries == null || durationEntries.isEmpty() || durationEntries.contains(null);
        }
    }

    /**
     * Calculate the median of a list of duration entries
     */
    static class MedianUsageStatisticStrategy implements UsageStatisticStrategy {
        @Override
        public Duration calculate(List<Duration> durationEntries) {
            if (verifyNoNulls(durationEntries)) {
                return ZERO;
            }
            Collections.sort(durationEntries);
            int middle = durationEntries.size() / 2;
            if (durationEntries.size() % 2 == 1) {
                return durationEntries.get(middle);
            } else {
                return (durationEntries.get(middle - 1).plus(durationEntries.get(middle))).dividedBy(2);
            }
        }

        private boolean verifyNoNulls(List<Duration> durationEntries) {
            return durationEntries == null || durationEntries.isEmpty() || durationEntries.contains(null);
        }
    }
}
