package io.cucumber.core.plugin;

import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.SummaryPrinter;

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
        printErrors();
        printSnippets();
        out.println();
    }

    private void printStats() {
        stats.printStats(out);
        out.println();
    }

    private void printErrors() {
        List<Throwable> errors = stats.getErrors();
        if (errors.isEmpty()) {
            return;
        }
        out.println();
        for (Throwable error : errors) {
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
            out.println();
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
