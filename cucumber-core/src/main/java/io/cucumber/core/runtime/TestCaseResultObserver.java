package io.cucumber.core.runtime;

import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.PENDING;
import static io.cucumber.plugin.event.Status.SKIPPED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class TestCaseResultObserver implements AutoCloseable {

    private final EventPublisher bus;
    private final List<Suggestion> suggestions = new ArrayList<>();
    private final EventHandler<SnippetsSuggestedEvent> snippetsSuggested = this::handleSnippetSuggestedEvent;
    private Result result;
    private final EventHandler<TestCaseFinished> testCaseFinished = this::handleTestCaseFinished;

    public TestCaseResultObserver(EventPublisher bus) {
        this.bus = bus;
        bus.registerHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggested);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinished);
    }

    @Override
    public void close() {
        bus.removeHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggested);
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinished);
    }

    private void handleSnippetSuggestedEvent(SnippetsSuggestedEvent event) {
        SnippetsSuggestedEvent.Suggestion s = event.getSuggestion();
        suggestions.add(new Suggestion(s.getStep(), s.getSnippets(), event.getUri(), event.getStepLocation()));
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

    public static final class Suggestion {

        final String step;
        final List<String> snippets;
        final URI uri;
        final Location location;

        @Deprecated
        public Suggestion(String step, List<String> snippets) {
            this.step = requireNonNull(step);
            this.snippets = unmodifiableList(requireNonNull(snippets));
            this.uri = null;
            this.location = null;
        }

        public Suggestion(String step, List<String> snippets, URI uri, Location location) {
            this.step = requireNonNull(step);
            this.snippets = unmodifiableList(requireNonNull(snippets));
            this.uri = requireNonNull(uri);
            this.location = requireNonNull(location);
        }

        public String getStep() {
            return step;
        }

        public List<String> getSnippets() {
            return snippets;
        }

        public URI getUri() {
            return uri;
        }

        public Location getLocation() {
            return location;
        }
    }

}
