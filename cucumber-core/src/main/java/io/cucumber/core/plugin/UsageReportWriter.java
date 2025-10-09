package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.query.Query;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

final class UsageReportWriter {

    private final Query query;
    private final Function<String, String> uriFormatter;
    private final SourceReferenceFormatter sourceReferenceFormatter;

    UsageReportWriter(Query query, Function<String, String> uriFormatter) {
        this.query = requireNonNull(query);
        this.uriFormatter = requireNonNull(uriFormatter);
        this.sourceReferenceFormatter = new SourceReferenceFormatter(uriFormatter);
    }

    UsageReport createUsageReport() {
        Map<Optional<StepDefinition>, List<StepUsage>> testStepsFinishedByStepDefinition = query
                .findAllTestStepFinished()
                .stream()
                .collect(groupingBy(findUnambiguousStepDefinitionBy(), LinkedHashMap::new,
                    mapping(createStepDuration(), toList())));

        // Add unused step definitions
        query.findAllStepDefinitions().stream()
                .map(Optional::of)
                .forEach(stepDefinition -> testStepsFinishedByStepDefinition
                        .computeIfAbsent(stepDefinition, sd -> new ArrayList<>()));

        List<StepDefinitionUsage> stepDefinitionUsages = testStepsFinishedByStepDefinition.entrySet()
                .stream()
                // Filter out steps with without a step definition or with an
                // ambiguous step definition. These can't be represented.
                .filter(entry -> entry.getKey().isPresent())
                .map(entry -> createStepContainer(entry.getKey().get(), entry.getValue()))
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
        Duration mean = sum.dividedBy(stepUsages.size());
        Duration moe95 = calculateMarginOfError95(stepUsages, mean);
        return new Statistics(sum, mean, moe95);
    }

    /**
     * Calculate the margin of error with a 0.95% confidence interval.
     */
    private static Duration calculateMarginOfError95(List<StepUsage> stepUsages, Duration mean) {
        BigDecimal meanSeconds = toBigDecimalSeconds(mean);
        BigDecimal variance = stepUsages.stream()
                .map(StepUsage::getDuration)
                .map(UsageReportWriter::toBigDecimalSeconds)
                .map(durationSeconds -> durationSeconds.subtract(meanSeconds).pow(2))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        // TODO: With Java 17, use BigDecimal.sqrt and
        // BigDecimal.divideAndRemainder for seconds and nos
        double marginOfError = 2 * Math.sqrt(variance.doubleValue()) / stepUsages.size();
        long seconds = (long) Math.floor(marginOfError);
        long nanos = (long) Math.floor((marginOfError - seconds) * TimeUnit.SECONDS.toNanos(1));
        return Duration.ofSeconds(seconds, nanos);
    }

    private static BigDecimal toBigDecimalSeconds(Duration duration) {
        return BigDecimal.valueOf(duration.getSeconds()).add(BigDecimal.valueOf(duration.getNano(), 9));
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

    private Function<TestStepFinished, Optional<StepDefinition>> findUnambiguousStepDefinitionBy() {
        return testStepFinished -> query.findTestStepBy(testStepFinished)
                .flatMap(query::findUnambiguousStepDefinitionBy);
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
        private final Duration mean;
        private final Duration moe95;

        Statistics(Duration sum, Duration mean, Duration moe95) {
            this.sum = sum;
            this.mean = mean;
            this.moe95 = moe95;
        }

        public Duration getSum() {
            return sum;
        }

        public Duration getMean() {
            return mean;
        }

        public Duration getMoe95() {
            return moe95;
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
