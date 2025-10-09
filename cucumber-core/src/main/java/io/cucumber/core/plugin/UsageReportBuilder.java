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

final class UsageReportBuilder {

    private final Query query;
    private final Function<String, String> uriFormatter;
    private final SourceReferenceFormatter sourceReferenceFormatter;

    UsageReportBuilder(Query query, Function<String, String> uriFormatter) {
        this.query = requireNonNull(query);
        this.uriFormatter = requireNonNull(uriFormatter);
        this.sourceReferenceFormatter = new SourceReferenceFormatter(uriFormatter);
    }

    UsageReport build() {
        Map<Optional<StepDefinition>, List<UsageReport.StepUsage>> testStepsFinishedByStepDefinition = query
                .findAllTestStepFinished()
                .stream()
                .collect(groupingBy(findUnambiguousStepDefinitionBy(), LinkedHashMap::new,
                    mapping(createStepDuration(), toList())));

        // Add unused step definitions
        query.findAllStepDefinitions().stream()
                .map(Optional::of)
                .forEach(stepDefinition -> testStepsFinishedByStepDefinition
                        .computeIfAbsent(stepDefinition, sd -> new ArrayList<>()));

        List<UsageReport.StepDefinitionUsage> stepDefinitionUsages = testStepsFinishedByStepDefinition.entrySet()
                .stream()
                // Filter out steps with without a step definition or with an
                // ambiguous step definition. These can't be represented.
                .filter(entry -> entry.getKey().isPresent())
                .map(entry -> createStepContainer(entry.getKey().get(), entry.getValue()))
                .collect(toList());
        return new UsageReport(stepDefinitionUsages);
    }

    private UsageReport.StepDefinitionUsage createStepContainer(StepDefinition stepDefinition, List<UsageReport.StepUsage> stepUsages) {
        UsageReport.Statistics aggregatedDurations = createDurationStatistics(stepUsages);
        String pattern = stepDefinition.getPattern().getSource();
        String location = sourceReferenceFormatter.format(stepDefinition.getSourceReference()).orElse("");
        return new UsageReport.StepDefinitionUsage(pattern, location, aggregatedDurations, stepUsages);
    }

    private static UsageReport.Statistics createDurationStatistics(List<UsageReport.StepUsage> stepUsages) {
        if (stepUsages.isEmpty()) {
            return null;
        }
        Duration sum = stepUsages.stream()
                .map(UsageReport.StepUsage::getDuration)
                .reduce(Duration::plus)
                // Can't happen
                .orElse(Duration.ZERO);
        Duration mean = sum.dividedBy(stepUsages.size());
        Duration moe95 = calculateMarginOfError95(stepUsages, mean);
        return new UsageReport.Statistics(sum, mean, moe95);
    }

    /**
     * Calculate the margin of error with a 0.95% confidence interval.
     */
    private static Duration calculateMarginOfError95(List<UsageReport.StepUsage> stepUsages, Duration mean) {
        BigDecimal meanSeconds = toBigDecimalSeconds(mean);
        BigDecimal variance = stepUsages.stream()
                .map(UsageReport.StepUsage::getDuration)
                .map(UsageReportBuilder::toBigDecimalSeconds)
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

    private Function<TestStepFinished, UsageReport.StepUsage> createStepDuration() {
        return testStepFinished -> query
                .findTestStepBy(testStepFinished)
                .flatMap(query::findPickleStepBy)
                .map(pickleStep -> createStepDuration(testStepFinished, pickleStep))
                .orElseGet(() -> new UsageReport.StepUsage("", Duration.ZERO, ""));
    }

    private UsageReport.StepUsage createStepDuration(TestStepFinished testStepFinished, PickleStep pickleStep) {
        String text = pickleStep.getText();
        String location = findLocationOf(testStepFinished);
        Duration duration = Convertor.toDuration(testStepFinished.getTestStepResult().getDuration());
        return new UsageReport.StepUsage(text, duration, location);
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

}
