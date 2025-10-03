package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepDefinitionPattern;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.query.Query;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.cucumber.messages.types.StepDefinitionPatternType.CUCUMBER_EXPRESSION;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

final class UsageReportWriter {

    private final Query query;
    private final Function<String, String> uriFormatter = s -> s;
    private final SourceReferenceFormatter sourceReferenceFormatter;

    UsageReportWriter(Query query) {
        this.query = query;
        this.sourceReferenceFormatter = new SourceReferenceFormatter(uriFormatter);
    }

    UsageReport createUsageReport() {
        Map<StepDefinition, List<StepUsage>> testStepsFinishedByStepDefinition = query.findAllTestStepFinished()
                .stream()
                .collect(groupingBy(findUnambiguousStepDefinitionBy(), LinkedHashMap::new,
                    mapping(createStepDuration(), toList())));

        // Add unused step definitions
        query.findAllStepDefinitions().forEach(stepDefinition -> testStepsFinishedByStepDefinition
                .computeIfAbsent(stepDefinition, sd -> new ArrayList<>()));

        List<StepDefinitionUsage> stepDefinitionUsages = testStepsFinishedByStepDefinition.entrySet()
                .stream()
                .map(entry -> createStepContainer(entry.getKey(), entry.getValue()))
                .collect(toList());
        return new UsageReport(stepDefinitionUsages);
    }

    private StepDefinitionUsage createStepContainer(StepDefinition stepDefinition, List<StepUsage> stepUsages) {
        Statistics aggregatedDurations = createDurationStatistics(stepUsages);
        String pattern = stepDefinition.getPattern().getSource();
        String location = sourceReferenceFormatter.format(stepDefinition.getSourceReference()).orElse("");
        return new StepDefinitionUsage(pattern, location, aggregatedDurations, stepUsages);
    }

    private static Statistics createDurationStatistics(List<StepUsage> stepUsages) {
        if (stepUsages.isEmpty()) {
            return null;
        }
        Duration sum = stepUsages.stream()
                .map(StepUsage::getDuration)
                .reduce(Duration::plus)
                // Can't happen
                .orElse(Duration.ZERO);

        Duration min = stepUsages.stream()
                .map(StepUsage::getDuration)
                .min(naturalOrder())
                // Can't happen
                .orElse(Duration.ZERO);

        Duration max = stepUsages.stream()
                .map(StepUsage::getDuration)
                .max(naturalOrder())
                // Can't happen
                .orElse(Duration.ZERO);

        Duration average = sum.dividedBy(stepUsages.size());

        Duration median = getMedian(stepUsages);

        return new Statistics(sum, average, median, min, max);
    }

    private static Duration getMedian(List<StepUsage> stepUsages) {
        long size = stepUsages.size();
        long medianItems = size % 2 == 0 ? 2 : 1;
        long medianIndex = size % 2 == 0 ? (size / 2) - 1 : size / 2;
        return stepUsages.stream()
                .map(StepUsage::getDuration)
                .sorted()
                .skip(medianIndex)
                .limit(medianItems)
                .reduce(Duration::plus)
                .orElse(Duration.ZERO)
                .dividedBy(medianItems);
    }

    private Function<TestStepFinished, StepUsage> createStepDuration() {
        return testStepFinished -> query
                .findTestStepBy(testStepFinished)
                .flatMap(query::findPickleStepBy)
                .map(pickleStep -> createStepDuration(testStepFinished, pickleStep))
                .orElseGet(() -> new StepUsage("", Duration.ZERO, ""));
    }

    private StepUsage createStepDuration(TestStepFinished testStepFinished, PickleStep pickleStep) {
        String text = pickleStep.getText();
        String location = findLocationOf(testStepFinished);
        Duration duration = Convertor.toDuration(testStepFinished.getTestStepResult().getDuration());
        return new StepUsage(text, duration, location);
    }

    private String findLocationOf(TestStepFinished testStepFinished) {
        return query.findPickleBy(testStepFinished)
                .map(pickle -> uriFormatter.apply(pickle.getUri()) + query.findLocationOf(pickle)
                        .map(Location::getLine)
                        .map(line -> ":" + line)
                        .orElse(""))
                .orElse("");
    }

    private Function<TestStepFinished, StepDefinition> findUnambiguousStepDefinitionBy() {
        return testStepFinished -> query.findTestStepBy(testStepFinished)
                .flatMap(query::findUnambiguousStepDefinitionBy)
                .orElseGet(UsageReportWriter::createDummyStepDefinition);
    }

    private static StepDefinition createDummyStepDefinition() {
        return new StepDefinition("", new StepDefinitionPattern("", CUCUMBER_EXPRESSION), SourceReference.of(""));
    }

    static final class UsageReport {
        private final List<StepDefinitionUsage> stepDefinitions;

        UsageReport(List<StepDefinitionUsage> stepDefinitions) {
            this.stepDefinitions = requireNonNull(stepDefinitions);
        }

        public List<StepDefinitionUsage> getStepDefinitions() {
            return stepDefinitions;
        }
    }

    /**
     * Container for usage-entries of steps
     */
    static final class StepDefinitionUsage {

        private final String expression;
        private final String location;
        private final Statistics duration;
        private final List<StepUsage> steps;

        StepDefinitionUsage(
                String expression, String location, Statistics duration, List<StepUsage> steps
        ) {
            this.expression = requireNonNull(expression);
            this.location = requireNonNull(location);
            this.duration = duration;
            this.steps = requireNonNull(steps);
        }

        public String getExpression() {
            return expression;
        }

        public Statistics getDuration() {
            return duration;
        }

        public List<StepUsage> getSteps() {
            return steps;
        }

        public String getLocation() {
            return location;
        }
    }

    static final class Statistics {
        private final Duration sum;
        private final Duration average;
        private final Duration median;
        private final Duration min;
        private final Duration max;

        Statistics(Duration sum, Duration average, Duration median, Duration min, Duration max) {
            this.sum = sum;
            this.average = average;
            this.median = median;
            this.min = min;
            this.max = max;
        }

        public Duration getSum() {
            return sum;
        }

        public Duration getAverage() {
            return average;
        }

        public Duration getMedian() {
            return median;
        }

        public Duration getMin() {
            return min;
        }

        public Duration getMax() {
            return max;
        }
    }

    static final class StepUsage {

        private final String text;
        private final Duration duration;
        private final String location;

        StepUsage(String text, Duration duration, String location) {
            this.text = requireNonNull(text);
            this.duration = requireNonNull(duration);
            this.location = requireNonNull(location);
        }

        public Duration getDuration() {
            return duration;
        }

        public String getLocation() {
            return location;
        }

        public String getText() {
            return text;
        }
    }

}
