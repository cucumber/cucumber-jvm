package io.cucumber.testng;

import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.SnippetsSuggestedEvent;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.event.TestStep;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.eventbus.EventBus;
import org.testng.SkipException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.cucumber.core.event.Status.UNDEFINED;

final class TestCaseResultListener {

    private static final String SKIPPED_MESSAGE = "This scenario is skipped";
    private final EventBus bus;
    private final Map<StepLocation, List<String>> snippetsPerStep = new TreeMap<>();
    private final EventHandler<SnippetsSuggestedEvent> snippetsSuggestedHandler = this::snippetSuggested;
    private boolean strict;
    private Result result;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = this::receiveResult;
    private PickleStepTestStep skippedStep;
    private final EventHandler<TestStepFinished> testStepFinished = this::testStepFinished;

    TestCaseResultListener(EventBus bus, boolean strict) {
        this.strict = strict;
        this.bus = bus;
        bus.registerHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggestedHandler);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinished);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    private void testStepFinished(TestStepFinished event) {
        if (!event.getResult().getStatus().is(UNDEFINED)) {
            return;
        }
        TestStep testStep = event.getTestStep();
        if (!(testStep instanceof PickleStepTestStep)) {
            return;
        }
        skippedStep = (PickleStepTestStep) testStep;
    }

    private void receiveResult(TestCaseFinished event) {
        this.result = event.getResult();
    }

    private void snippetSuggested(SnippetsSuggestedEvent snippetsSuggestedEvent) {
        snippetsPerStep.putIfAbsent(new StepLocation(
                snippetsSuggestedEvent.getUri(),
                snippetsSuggestedEvent.getStepLine()
            ),
            snippetsSuggestedEvent.getSnippets()
        );
    }

    void finishExecutionUnit() {
        bus.removeHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggestedHandler);
        bus.removeHandlerFor(TestStepFinished.class, testStepFinished);
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    boolean isPassed() {
        return result == null || result.getStatus().is(Status.PASSED);
    }

    Throwable getError() {
        if (result == null) {
            return null;
        }
        switch (result.getStatus()) {
            case FAILED:
            case AMBIGUOUS:
                return result.getError();
            case PENDING:
                return handlePendingResult();
            case UNDEFINED:
                return createUndefinedException();
            case SKIPPED:
                return handleSkippedResult();
            case PASSED:
                return null;
            default:
                throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
    }

    private Throwable handleSkippedResult() {
        Throwable error = result.getError();
        if (error == null) {
            return new SkipException(SKIPPED_MESSAGE);
        }
        if (error instanceof SkipException) {
            return error;
        }
        return new SkipException(result.getError().getMessage(), error);
    }

    private Throwable handlePendingResult() {
        if (strict) {
            return result.getError();
        }
        return new SkipException(result.getError().getMessage(), result.getError());
    }

    private Throwable createUndefinedException() {
        List<String> snippets = snippetsPerStep.remove(
            new StepLocation(skippedStep.getUri(), skippedStep.getStepLine())
        );

        return new UndefinedStepException(
            skippedStep.getStepText(),
            snippets,
            snippetsPerStep.values(),
            strict
        );
    }

    private static final class StepLocation implements Comparable<StepLocation> {
        private final String uri;
        private final int line;

        private StepLocation(String uri, int line) {
            this.uri = uri;
            this.line = line;
        }

        @Override
        public int compareTo(StepLocation o) {
            int order = uri.compareTo(o.uri);
            return order != 0 ? order : Integer.compare(line, o.line);
        }
    }

}
