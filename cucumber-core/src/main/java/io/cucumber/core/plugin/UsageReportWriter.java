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
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.cucumber.messages.types.StepDefinitionPatternType.CUCUMBER_EXPRESSION;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

final class UsageReportWriter {

    private final Query query;
    private final Function<String, String> uriFormatter;
    private final SourceReferenceFormatter sourceReferenceFormatter;

    UsageReportWriter(Query query, Function<String, String> uriFormatter) {
        this.query = query;
        this.uriFormatter = uriFormatter;
        this.sourceReferenceFormatter = new SourceReferenceFormatter(uriFormatter);
    }


    List<StepDefinitionUsage> createUsageReport() {
        Map<StepDefinition, List<StepUsage>> testStepsFinishedByStepDefinition = query.findAllTestStepFinished()
                .stream()
                .collect(groupingBy(findUnambiguousStepDefinitionBy(), LinkedHashMap::new,
                        mapping(createStepDuration(), toList())));

        testStepsFinishedByStepDefinition.values()
                .forEach((stepUsages) -> stepUsages.sort(comparing(StepUsage::getDuration).reversed()));

        // Add unused step definitions
        query.findAllStepDefinitions().forEach(stepDefinition -> testStepsFinishedByStepDefinition
                .computeIfAbsent(stepDefinition, sd -> new ArrayList<>()));

        return testStepsFinishedByStepDefinition.entrySet()
                .stream()
                .map(entry -> createStepContainer(entry.getKey(), entry.getValue()))
                .sorted(comparing(StepDefinitionUsage::getStatistics, nullsFirst(comparing(Statistics::getAverage))).reversed())
                .collect(toList());
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
        DoubleSummaryStatistics stats = stepUsages.stream()
                .mapToDouble(StepUsage::getDuration)
                .summaryStatistics();
        double median = getMedian(stepUsages);
        return new Statistics(stats.getSum(), stats.getAverage(), median, stats.getMin(), stats.getMax());
    }

    private static double getMedian(List<StepUsage> stepUsages) {
        long size = stepUsages.size();
        long medianItems = size % 2 == 0 ? 2 : 1;
        long medianIndex = size % 2 == 0 ? (size / 2) - 1 : size / 2;
        return stepUsages.stream()
                .mapToDouble(StepUsage::getDuration)
                .sorted()
                .skip(medianIndex)
                .limit(medianItems)
                .average()
                .orElse(0.0);
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

    /**
     * Container for usage-entries of steps
     */
    static final class StepDefinitionUsage {

        private final String expression;
        private final String location;
        private final Statistics statistics;
        private final List<StepUsage> usages;

        StepDefinitionUsage(
                String expression, String location, Statistics statistics, List<StepUsage> usages
        ) {
            this.expression = requireNonNull(expression);
            this.location = requireNonNull(location);
            this.statistics = statistics;
            this.usages = requireNonNull(usages);
        }

        public String getExpression() {
            return expression;
        }

        public Statistics getStatistics() {
            return statistics;
        }

        public List<StepUsage> getUsages() {
            return usages;
        }

        public String getLocation() {
            return location;
        }
    }

    static final class Statistics {
        private final double sum;
        private final double average;
        private final double median;
        private final double min;
        private final double max;

        Statistics(double sum, double average, double median, double min, double max) {
            this.sum = sum;
            this.average = average;
            this.median = median;
            this.min = min;
            this.max = max;
        }

        public double getSum() {
            return sum;
        }

        public double getAverage() {
            return average;
        }

        public double getMedian() {
            return median;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }

    private static double durationToSeconds(Duration duration) {
        return (double) duration.toNanos() / TimeUnit.SECONDS.toNanos(1);
    }

    static final class StepUsage {

        private final String text;
        private final double duration;
        private final String location;

        StepUsage(String text, Duration duration, String location) {
            this.text = text;
            this.duration = durationToSeconds(duration);
            this.location = location;
        }

        public double getDuration() {
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
