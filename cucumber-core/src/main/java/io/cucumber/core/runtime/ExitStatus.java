package io.cucumber.core.runtime;

import io.cucumber.core.plugin.Options;
import io.cucumber.messages.TestStepResultStatusComparator;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunHookFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Status;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.util.Optional;
import java.util.stream.Stream;

import static io.cucumber.messages.types.TestStepResultStatus.FAILED;
import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static io.cucumber.messages.types.TestStepResultStatus.SKIPPED;

public final class ExitStatus implements ConcurrentEventListener {

    private static final byte DEFAULT = 0x0;
    private static final byte ERRORS = 0x1;

    private final Repository repository = Repository.builder().build();
    private final Query query = new Query(repository);
    private final Options options;

    public ExitStatus(Options options) {
        this.options = options;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, repository::update);
    }

    byte exitStatus() {
        return isSuccess() ? DEFAULT : ERRORS;
    }

    boolean isSuccess() {
        if (options.isWip()) {
            var leastSeverResult = getLeastSeverStatus();
            return leastSeverResult != PASSED;
        }
        var mostSevereResult = getMostSevereStatus();
        return mostSevereResult == PASSED || mostSevereResult == SKIPPED;
    }

    private TestStepResultStatus getLeastSeverStatus() {
        return query.findAllTestCaseStarted().stream()
                .map(query::findMostSevereTestStepResultBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TestStepResult::getStatus)
                .min(new TestStepResultStatusComparator())
                .orElse(FAILED);
    }

    private TestStepResultStatus getMostSevereStatus() {
        var testRunHookFinishedStatusResults = query.findAllTestRunHookFinished().stream()
                .map(TestRunHookFinished::getResult)
                .map(TestStepResult::getStatus);

        var testStepStatusResults = query.findAllTestCaseStarted().stream()
                .map(query::findMostSevereTestStepResultBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TestStepResult::getStatus);

        return Stream.concat(testRunHookFinishedStatusResults, testStepStatusResults)
                .max(new TestStepResultStatusComparator())
                .orElse(PASSED);
    }

    Status getStatus() {
        var testStepResultStatus = getMostSevereStatus();
        return switch (testStepResultStatus) {
            case PASSED -> Status.PASSED;
            case SKIPPED -> Status.SKIPPED;
            case PENDING -> Status.PENDING;
            case UNDEFINED -> Status.UNDEFINED;
            case AMBIGUOUS -> Status.AMBIGUOUS;
            case FAILED -> Status.FAILED;
            default -> throw new IllegalStateException("Unexpected value: " + testStepResultStatus);
        };
    }

}
