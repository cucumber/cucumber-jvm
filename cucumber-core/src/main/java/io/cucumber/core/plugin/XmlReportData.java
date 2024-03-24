package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class XmlReportData {

    private final Query query = new Query();

    void collect(Envelope envelope) {
        query.update(envelope);
    }

    long getSuiteDurationInMilliSeconds() {
        return query.findTestRunDuration()
                .orElse(Duration.ZERO)
                .toMillis();
    }

    long getDurationInMilliSeconds(TestCaseStarted testCaseStarted) {
        return query.findTestCaseDurationBy(testCaseStarted)
                .orElse(Duration.ZERO)
                .toMillis();
    }

    Map<TestStepResultStatus, Long> getTestCaseStatusCounts() {
        return query.findMostSevereTestStepResultStatusCount();
    }

    int getTestCaseCount() {
        return query.findAllTestCaseStarted().size();
    }

    String getPickleName(TestCaseStarted testCaseStarted) {
        Pickle pickle = query.findPickleBy(testCaseStarted)
                .orElseThrow(() -> new IllegalStateException("No pickle for " + testCaseStarted.getId()));

        return query.findGherkinAstNodesBy(pickle)
                .map(XmlReportData::getPickleName)
                .orElse(pickle.getName());
    }

    private static String getPickleName(GherkinAstNodes elements) {
        List<String> pieces = new ArrayList<>();

        elements.rule().map(Rule::getName).ifPresent(pieces::add);

        pieces.add(elements.scenario().getName());

        elements.examples().map(Examples::getName).ifPresent(pieces::add);

        String examplesPrefix = elements.examplesIndex()
                .map(examplesIndex -> examplesIndex + 1)
                .map(examplesIndex -> examplesIndex + ".")
                .orElse("");

        elements.exampleIndex()
                .map(exampleIndex -> exampleIndex + 1)
                .map(exampleSuffix -> "Example #" + examplesPrefix + exampleSuffix)
                .ifPresent(pieces::add);

        return pieces.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" - "));
    }

    List<Entry<String, String>> getStepsAndResult(TestCaseStarted testCaseStarted) {
        return query.findTestStepAndTestStepFinishedBy(testCaseStarted)
                .stream()
                // Exclude hooks
                .filter(entry -> entry.getKey().getPickleStepId().isPresent())
                .map(testStep -> {
                    String key = renderTestStepText(testStep.getKey());
                    String value = renderTestStepResult(testStep.getValue());
                    return new SimpleEntry<>(key, value);
                })
                .collect(toList());
    }

    private String renderTestStepResult(TestStepFinished testStepFinished) {
        return testStepFinished
                .getTestStepResult()
                .getStatus()
                .toString()
                .toLowerCase(Locale.ROOT);
    }

    private String renderTestStepText(TestStep testStep) {
        Optional<PickleStep> pickleStep = query.findPickleStepBy(testStep);

        String stepKeyWord = pickleStep
                .flatMap(query::findStepBy)
                .map(Step::getKeyword)
                .orElse("");

        String stepText = pickleStep
                .map(PickleStep::getText)
                .orElse("");

        return stepKeyWord + stepText;
    }

    Map<Optional<Feature>, List<TestCaseStarted>> getAllTestCaseStartedGroupedByFeature() {
        return query.findAllTestCaseStarted()
                .stream()
                .map(testCaseStarted -> {
                    Optional<GherkinAstNodes> astNodes = query.findGherkinAstNodesBy(testCaseStarted);
                    return new SimpleEntry<>(astNodes, testCaseStarted);
                })
                // Sort by URI for consistent ordering
                .sorted(
                        nullsFirst(comparing(entry -> entry.getKey()
                                .flatMap(nodes -> nodes.document().getUri())
                                .orElse(null))))
                .map(entry -> new SimpleEntry<>(entry.getKey().map(GherkinAstNodes::feature), entry.getValue()))
                .collect(groupingBy(SimpleEntry::getKey, LinkedHashMap::new,
                        collectingAndThen(Collectors.toList(), entries -> entries.stream()
                                .map(SimpleEntry::getValue)
                                .collect(toList()))));
    }

    private static final io.cucumber.messages.types.Duration ZERO_DURATION =
            new io.cucumber.messages.types.Duration(0L, 0L);
    // By definition, but see https://github.com/cucumber/gherkin/issues/11
    private static final TestStepResult SCENARIO_WITH_NO_STEPS = new TestStepResult(ZERO_DURATION, null, PASSED, null);

    TestStepResult getTestCaseStatus(TestCaseStarted testCaseStarted) {
        return query.findMostSevereTestStepResulBy(testCaseStarted)
                .orElse(SCENARIO_WITH_NO_STEPS);
    }

    public String getStartedAt(TestCaseStarted testCaseStarted) {
        Instant instant = Convertor.toInstant(testCaseStarted.getTimestamp());
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    public String getFinishedAt(TestCaseStarted testCaseStarted) {
        TestCaseFinished testCaseFinished = query.findTestCaseFinishedBy(testCaseStarted)
                .orElseThrow(() -> new IllegalStateException("No test cased finished for " + testCaseStarted));
        Instant instant = Convertor.toInstant(testCaseFinished.getTimestamp());
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
