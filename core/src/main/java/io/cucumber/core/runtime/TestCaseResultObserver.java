package io.cucumber.core.runtime;

import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.PENDING;
import static io.cucumber.plugin.event.Status.SKIPPED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static java.util.Objects.requireNonNull;

public final class TestCaseResultObserver implements AutoCloseable {

    private final EventPublisher bus;
    private final Map<StepLocation, List<String>> snippetsPerStep = new TreeMap<>();
    private final List<Suggestion> suggestions = new ArrayList<>();
    private final EventHandler<SnippetsSuggestedEvent> snippetsSuggested = this::handleSnippetSuggestedEvent;
    private final EventHandler<TestStepFinished> testStepFinished = this::handleTestStepFinished;
    private Result result;
    private final EventHandler<TestCaseFinished> testCaseFinished = this::handleTestCaseFinished;

    public TestCaseResultObserver(EventPublisher bus) {
        this.bus = bus;
        bus.registerHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggested);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinished);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinished);
    }

    @Override
    public void close() {
        bus.removeHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggested);
        bus.removeHandlerFor(TestStepFinished.class, testStepFinished);
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinished);
    }

    private void handleSnippetSuggestedEvent(SnippetsSuggestedEvent event) {
        snippetsPerStep.putIfAbsent(new StepLocation(
            event.getUri(),
            event.getStepLine()),
            event.getSnippets());
    }

    private void handleTestStepFinished(TestStepFinished event) {
        Result result = event.getResult();
        Status status = result.getStatus();
        if (!status.is(UNDEFINED)) {
            return;
        }

        TestStep testStep = event.getTestStep();
        if (!(testStep instanceof PickleStepTestStep)) {
            return;
        }

        PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
        String stepText = pickleStepTestStep.getStepText();

        List<String> snippets = snippetsPerStep.get(
            new StepLocation(
                pickleStepTestStep.getUri(),
                pickleStepTestStep.getStepLine()));
        suggestions.add(new Suggestion(stepText, snippets));
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        result = event.getResult();
    }

    public void assertTestCasePassed(
            Supplier<Throwable> testCaseSkipped,
            Function<Throwable, Throwable> testCaseSkippedWithException,
            Function<List<Suggestion>, Throwable> testCaseWasUndefined,
            Function<Throwable, Throwable> testCaseWasPending
    ) {
        Status status = result.getStatus();
        if (status.is(PASSED)) {
            return;
        }
        Throwable error = result.getError();
        if (status.is(SKIPPED) && error == null) {
            Throwable throwable = testCaseSkipped.get();
            throw new TestCaseFailed(throwable);
        } else if (status.is(SKIPPED) && error != null) {
            Throwable throwable = testCaseSkippedWithException.apply(error);
            throw new TestCaseFailed(throwable);
        } else if (status.is(UNDEFINED)) {
            Throwable throwable = testCaseWasUndefined.apply(suggestions);
            throw new TestCaseFailed(throwable);
        } else if (status.is(PENDING)) {
            Throwable throwable = testCaseWasPending.apply(error);
            throw new TestCaseFailed(throwable);
        }
        requireNonNull(error, "result.error=null while result.status=" + result.getStatus());
        throw new TestCaseFailed(error);
    }

    static class TestCaseFailed extends RuntimeException {

        public TestCaseFailed(Throwable throwable) {
            super(throwable);
        }

    }

    private static final class StepLocation implements Comparable<StepLocation> {

        private final URI uri;
        private final int line;

        private StepLocation(URI uri, int line) {
            this.uri = uri;
            this.line = line;
        }

        @Override
        public int compareTo(StepLocation o) {
            int order = uri.compareTo(o.uri);
            return order != 0 ? order : Integer.compare(line, o.line);
        }

    }

    public static final class Suggestion {

        final String step;
        final List<String> snippets;

        public Suggestion(String step, List<String> snippets) {
            this.step = step;
            this.snippets = snippets;
        }

        public String getStep() {
            return step;
        }

        public List<String> getSnippets() {
            return snippets;
        }

    }

}
