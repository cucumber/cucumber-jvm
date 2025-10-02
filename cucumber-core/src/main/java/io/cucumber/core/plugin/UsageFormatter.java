package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_STEP_DEFINITIONS;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UsageFormatter implements Plugin, ConcurrentEventListener {

    // TODO: Make uri formatter configurable.
    public final Function<String, String> uriFormatter = s -> s;
    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
            .feature(INCLUDE_STEP_DEFINITIONS, true)
            .build();
    private final Query query = new Query(repository);
    private final UTF8OutputStreamWriter out;
    private final SourceReferenceFormatter sourceReferenceFormatter = new SourceReferenceFormatter(uriFormatter);

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
        publisher.registerHandlerFor(Envelope.class, envelope -> {
            repository.update(envelope);
            envelope.getTestRunFinished().ifPresent(testRunFinished -> finishReport());
        });

    }

    void finishReport() {
        Map<StepDefinition, List<StepDuration>> testStepsFinishedByStepDefinition = query.findAllTestStepFinished()
                .stream()
                .collect(groupingBy(findUnambiguousStepDefinitionBy(), LinkedHashMap::new,
                    mapping(createStepDuration(), toList())));

        // Include unused
        query.findAllStepDefinitions().forEach(stepDefinition -> testStepsFinishedByStepDefinition
                .computeIfAbsent(stepDefinition, stepDefinition1 -> new ArrayList<>()));

        List<StepContainer> stepContainers = testStepsFinishedByStepDefinition.entrySet()
                .stream()
                .map(entry -> {
                    StepDefinition stepDefinition = entry.getKey();
                    List<StepDuration> stepDurations = entry.getValue();
                    DurationStatistics aggregatedDurations = createDurationStatistics(stepDurations);
                    String pattern = stepDefinition.getPattern().getSource();
                    String location = sourceReferenceFormatter.format(stepDefinition.getSourceReference()).orElse("");
                    return new StepContainer(pattern, location, aggregatedDurations, stepDurations);
                })
                .collect(toList());

        try {
            Jackson.OBJECT_MAPPER.writeValue(out, stepContainers);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DurationStatistics createDurationStatistics(List<StepDuration> stepDurations) {
        if (stepDurations.isEmpty()) {
            return null;
        }
        DoubleSummaryStatistics stats = stepDurations.stream()
                .mapToDouble(StepDuration::getDuration)
                .summaryStatistics();
        double median = getMedian(stepDurations);
        return new DurationStatistics(stats.getSum(), stats.getAverage(), median, stats.getMin(), stats.getMax());
    }

    private static double getMedian(List<StepDuration> stepDurations) {
        long size = stepDurations.size();
        long medianItems = size % 2 == 0 ? 2 : 1;
        long medianIndex = size % 2 == 0 ? (size / 2) - 1 : size / 2;
        return stepDurations.stream()
                .mapToDouble(StepDuration::getDuration)
                .sorted()
                .skip(medianIndex)
                .limit(medianItems)
                .average()
                .orElse(0.0);
    }

    private Function<TestStepFinished, StepDuration> createStepDuration() {
        return testStepFinished -> query
                .findTestStepBy(testStepFinished)
                .flatMap(query::findPickleStepBy)
                .map(pickleStep -> createStepDuration(testStepFinished, pickleStep))
                .orElseGet(() -> new StepDuration("", Duration.ZERO, ""));
    }

    private StepDuration createStepDuration(TestStepFinished testStepFinished, PickleStep pickleStep) {
        String text = pickleStep.getText();
        String location = findLocationOf(testStepFinished);
        Duration duration = Convertor.toDuration(testStepFinished.getTestStepResult().getDuration());
        return new StepDuration(text, duration, location);
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
                .get();
    }

    /**
     * Container for usage-entries of steps
     */
    static class StepContainer {

        private final String name;
        private final String location;
        private final DurationStatistics durationStatistics;
        private final List<StepDuration> durations;

        StepContainer(
                String name, String location, DurationStatistics durationStatistics, List<StepDuration> durations
        ) {
            this.name = requireNonNull(name);
            this.location = requireNonNull(location);
            this.durationStatistics = durationStatistics;
            this.durations = requireNonNull(durations);
        }

        public String getName() {
            return name;
        }

        public DurationStatistics getDurationStatistics() {
            return durationStatistics;
        }

        public List<StepDuration> getDurations() {
            return durations;
        }

        public String getLocation() {
            return location;
        }
    }

    static class DurationStatistics {
        private final double sum;
        private final double average;
        private final double median;
        private final double min;
        private final double max;

        DurationStatistics(double sum, double average, double median, double min, double max) {
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

    static class StepDuration {

        private final String text;
        private final double duration;
        private final String location;

        StepDuration(String text, Duration duration, String location) {
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
