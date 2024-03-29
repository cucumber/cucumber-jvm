package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.query.NamingStrategy;
import io.cucumber.query.Query;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static io.cucumber.query.NamingStrategy.FeatureName.EXCLUDE;
import static io.cucumber.query.NamingStrategy.Strategy.LONG;
import static java.util.stream.Collectors.toList;

class XmlReportData {

    final Query query = new Query();

    private final NamingStrategy namingStrategy = NamingStrategy
            .strategy(LONG)
            .featureName(EXCLUDE)
            .build();

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
        return query.countMostSevereTestStepResultStatus();
    }

    int getTestCaseCount() {
        return query.countTestCasesStarted();
    }

    String getPickleName(TestCaseStarted testCaseStarted) {
        Pickle pickle = query.findPickleBy(testCaseStarted)
                .orElseThrow(() -> new IllegalStateException("No pickle for " + testCaseStarted.getId()));

        return query.findNameOf(pickle, namingStrategy);
    }

    List<Entry<String, String>> getStepsAndResult(TestCaseStarted testCaseStarted) {
        return query.findTestStepFinishedAndTestStepBy(testCaseStarted)
                .stream()
                // Exclude hooks
                .filter(entry -> entry.getValue().getPickleStepId().isPresent())
                .map(testStep -> {
                    String key = renderTestStepText(testStep.getValue());
                    String value = renderTestStepResult(testStep.getKey());
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

    Set<Entry<Optional<Feature>, List<TestCaseStarted>>> getAllTestCaseStartedGroupedByFeature() {
        return query.findAllTestCaseStartedGroupedByFeature().entrySet();
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
