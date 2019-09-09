package io.cucumber.core.plugin;

import io.cucumber.core.event.EventPublisher;
import io.cucumber.core.event.SnippetsSuggestedEvent;
import io.cucumber.core.event.TestRunFinished;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public final class DefaultSummaryPrinter implements SummaryPrinter, ColorAware, StrictAware, ConcurrentEventListener {

    private final List<String> snippets = new ArrayList<>();
    private final Stats stats = new Stats();

    private final PrintStream out;

    public DefaultSummaryPrinter() {
        this.out = System.out;
    }

    private void handleSnippetsSuggestedEvent(SnippetsSuggestedEvent event) {
        this.snippets.addAll(event.getSnippets());
    }

    private void print() {
        out.println();
        printStats();
        out.println();
        printErrors();
        printSnippets();
    }

    private void printStats() {
        stats.printStats(out);
    }

    private void printErrors() {
        for (Throwable error : stats.getErrors()) {
            error.printStackTrace(out);
            out.println();
        }
    }

    private void printSnippets() {
        if (snippets.isEmpty()) {
            return;
        }

        out.println();
        out.println("You can implement missing steps with the snippets below:");
        out.println();
        for (String snippet : snippets) {
            out.println(snippet);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        stats.setEventPublisher(publisher);
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetsSuggestedEvent);
        publisher.registerHandlerFor(TestRunFinished.class, event -> print());
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        stats.setMonochrome(monochrome);
    }

    @Override
    public void setStrict(boolean strict) {
        stats.setStrict(strict);
    }
}
