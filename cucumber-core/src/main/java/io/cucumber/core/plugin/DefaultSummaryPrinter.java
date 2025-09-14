package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Exception;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Snippet;
import io.cucumber.messages.types.Suggestion;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.cucumber.core.plugin.Formats.ansi;
import static io.cucumber.core.plugin.Formats.monochrome;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_SUGGESTIONS;
import static java.util.Collections.emptyList;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public final class DefaultSummaryPrinter implements ColorAware, ConcurrentEventListener {

    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
            .feature(INCLUDE_SUGGESTIONS, true)
            .build();
    private final Query query = new Query(repository);
    private final PrintStream out;
    private Formats formats = ansi();

    public DefaultSummaryPrinter() {
        this(System.out);
    }

    DefaultSummaryPrinter(OutputStream out) {
        this.out = new PrintStream(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, envelope -> {
            repository.update(envelope);
            envelope.getTestRunFinished().ifPresent(testRunFinished -> print());
        });
    }

    private void print() {
        out.println();
        printStats();
        printErrors();
        printSnippets();
    }

    private void printStats() {
        printNonPassingScenarios();
        printScenarioCounts();
        printStepCounts();
        printDuration();
    }

    private void printNonPassingScenarios() {
        Map<TestStepResultStatus, List<TestCaseFinished>> testCaseFinishedByStatus = query
                .findAllTestCaseFinished()
                .stream()
                .collect(groupingBy(this::getTestStepResultStatusBy));

        printScenarios(testCaseFinishedByStatus, TestStepResultStatus.FAILED);
        printScenarios(testCaseFinishedByStatus, TestStepResultStatus.AMBIGUOUS);
        printScenarios(testCaseFinishedByStatus, TestStepResultStatus.PENDING);
        printScenarios(testCaseFinishedByStatus, TestStepResultStatus.UNDEFINED);
    }

    private void printScenarios(
            Map<TestStepResultStatus, List<TestCaseFinished>> testCaseFinishedByStatus,
            TestStepResultStatus type
    ) {
        List<TestCaseFinished> scenarios = testCaseFinishedByStatus.getOrDefault(type, emptyList());
        Format format = formats.get(type.name().toLowerCase(ROOT));
        if (!scenarios.isEmpty()) {
            out.println(format.text(firstLetterCapitalizedName(type) + " scenarios:"));
        }
        for (TestCaseFinished testCaseFinished : scenarios) {
            query.findPickleBy(testCaseFinished).ifPresent(pickle -> {
                String location = pickle.getUri()
                        + query.findLocationOf(pickle).map(Location::getLine).map(line -> ":" + line).orElse("");
                out.println(location + " # " + pickle.getName());
            });
        }
        if (!scenarios.isEmpty()) {
            out.println();
        }
    }

    private void printScenarioCounts() {
        out.println(formatSubCounts(
                "Scenarios", 
                query.findAllTestCaseFinished(), 
                countTestStepResultStatusByTestCaseFinished()));

    }

    private void printStepCounts() {
        out.println(formatSubCounts(
                "Steps", 
                query.findAllTestStepFinished(), 
                countTestStepResultStatusByTestStepFinished()));
    }

    private <T> String formatSubCounts(String itemName, List<T> finishedItems, Collector<T, ?, Map<TestStepResultStatus, Long>> countTestStepResultStatusByItem) {
        String countAndName = finishedItems.size() + " " + itemName;
        StringJoiner joiner = new StringJoiner(",", countAndName + " (",")");
        joiner.setEmptyValue(countAndName);
        Map<TestStepResultStatus, Long> subCounts = finishedItems.stream()
                .collect(countTestStepResultStatusByItem);
        for (TestStepResultStatus value : TestStepResultStatus.values()) {
            long count = subCounts.getOrDefault(value, 0L);
            if (count != 0) {
                Format format = formats.get(value.name().toLowerCase(ROOT));
                joiner.add(format.text(count + " " + value.name().toLowerCase(ROOT)));
            }
        }
        return joiner.toString();
    }

    private void printDuration() {
        query.findTestRunDuration()
                .map(DefaultSummaryPrinter::formatDuration)
                .ifPresent(out::println);
    }

    private static String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        long milliseconds = TimeUnit.NANOSECONDS.toMillis(duration.getNano());
        return String.format("%sm%s.%ss", minutes, seconds, milliseconds);
    }

    private void printErrors() {
        List<Exception> errors = query.findAllTestStepFinished()
                .stream()
                .map(TestStepFinished::getTestStepResult)
                .map(TestStepResult::getException)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        
        if (errors.isEmpty()) {
            return;
        }
        out.println();
        for (Exception error : errors) {
            out.println(error.getStackTrace());
            out.println();
        }
    }

    private void printSnippets() {
        Set<Snippet> snippets = query.findAllTestCaseFinished().stream()
                .map(query::findPickleBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(query::findSuggestionsBy)
                .flatMap(Collection::stream)
                .map(Suggestion::getSnippets)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (snippets.isEmpty()) {
            return;
        }

        out.println();
        out.println("You can implement missing steps with the snippets below:");
        out.println();
        for (Snippet snippet : snippets) {
            out.println(snippet.getCode());
            out.println();
        }
    }

    private Collector<TestCaseFinished, ?, Map<TestStepResultStatus, Long>> countTestStepResultStatusByTestCaseFinished() {
        return groupingBy(this::getTestStepResultStatusBy, counting());
    }

    private TestStepResultStatus getTestStepResultStatusBy(TestCaseFinished testCaseFinished) {
        return query.findMostSevereTestStepResultBy(testCaseFinished)
                .map(TestStepResult::getStatus)
                // By definition
                .orElse(TestStepResultStatus.PASSED);
    }

    private static Collector<TestStepFinished, ?, Map<TestStepResultStatus, Long>> countTestStepResultStatusByTestStepFinished() {
        return groupingBy(DefaultSummaryPrinter::getTestStepResultStatusBy, counting());
    }

    private static TestStepResultStatus getTestStepResultStatusBy(TestStepFinished testStepFinished) {
        return testStepFinished.getTestStepResult().getStatus();
    }

    private String firstLetterCapitalizedName(TestStepResultStatus status) {
        String name = status.name();
        return name.charAt(0) + name.substring(1).toLowerCase(ROOT);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        formats = monochrome ? monochrome() : ansi();
    }

}
