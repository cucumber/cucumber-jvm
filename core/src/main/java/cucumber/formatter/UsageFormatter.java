package cucumber.formatter;

import cucumber.runtime.StepDefinitionMatch;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formatter to measure performance of steps. Aggregated results for all steps can be computed
 * by adding {@link UsageStatisticStrategy} to the usageFormatter
 */
public class UsageFormatter implements Formatter, Reporter {
    public static final BigDecimal NANOS_PER_SECOND = BigDecimal.valueOf(1000000000);
    final Map<String, List<StepContainer>> usageMap = new HashMap<String, List<StepContainer>>();
    final Map<String, UsageStatisticStrategy> statisticStrategies = new HashMap<String, UsageStatisticStrategy>();

    private final NiceAppendable out;

    private Match match;

    /**
     * Constructor
     *
     * @param out {@link Appendable} to print the result
     */
    public UsageFormatter(Appendable out) {
        this.out = new NiceAppendable(out);

        addUsageStatisticStrategy("median", new MedianUsageStatisticStrategy());
        addUsageStatisticStrategy("average", new AverageUsageStatisticStrategy());
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void feature(Feature feature) {
    }

    @Override
    public void background(Background background) {
    }

    @Override
    public void scenario(Scenario scenario) {
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void embedding(String mimeType, InputStream data) {
    }

    @Override
    public void write(String text) {
    }

    @Override
    public void step(Step step) {
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void done() {
        List<StepDefContainer> stepDefContainers = new ArrayList<StepDefContainer>();
        for (Map.Entry<String, List<StepContainer>> usageEntry : usageMap.entrySet()) {
            StepDefContainer stepDefContainer = new StepDefContainer();
            stepDefContainers.add(stepDefContainer);

            stepDefContainer.source = usageEntry.getKey();
            stepDefContainer.steps = createStepContainer(usageEntry.getValue());
        }

        out.append(gson().toJson(stepDefContainers));
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
        Map<String, BigDecimal> aggregatedResults = new HashMap<String, BigDecimal>();
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

    @Override
    public void close() {
        out.close();
    }

    @Override
    public void result(Result result) {
        if (result.getStatus().equals(Result.PASSED)) {
            addUsageEntry(result, getStepDefinition(), getStepName());
        }
    }

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void after(Match match, Result result) {
    }

    private String getStepName() {
        return ((StepDefinitionMatch) match).getStepName();
    }

    private String getStepDefinition() {
        return ((StepDefinitionMatch) match).getPattern();
    }

    private void addUsageEntry(Result result, String stepDefinition, String stepNameWithArgs) {
        List<StepContainer> stepContainers = usageMap.get(stepDefinition);
        if (stepContainers == null) {
            stepContainers = new ArrayList<StepContainer>();
            usageMap.put(stepDefinition, stepContainers);
        }
        StepContainer stepContainer = findOrCreateStepContainer(stepNameWithArgs, stepContainers);

        String stepLocation = getStepLocation();
        Long duration = result.getDuration();
        StepDuration stepDuration = createStepDuration(duration, stepLocation);
        stepContainer.durations.add(stepDuration);
    }

    private String getStepLocation() {
        StackTraceElement stepLocation = ((StepDefinitionMatch) match).getStepLocation();
        return stepLocation.getFileName() + ":" + stepLocation.getLineNumber();
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

    @Override
    public void match(Match match) {
        this.match = match;
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
    static interface UsageStatisticStrategy {
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
