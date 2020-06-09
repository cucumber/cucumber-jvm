package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.SummaryPrinter;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.TestRunFinished;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DefaultSummaryPrinter implements SummaryPrinter, ColorAware, ConcurrentEventListener {

    private final Set<String> snippets = new LinkedHashSet<>();
    private final Stats stats;
    private final PrintStream out;

    public DefaultSummaryPrinter() {
        this(System.out, Locale.getDefault());
    }

    DefaultSummaryPrinter(OutputStream out, Locale locale) {
        this.out = new PrintStream(out);
        this.stats = new Stats(locale);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        stats.setEventPublisher(publisher);
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetsSuggestedEvent);
        publisher.registerHandlerFor(TestRunFinished.class, event -> print());
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
    public void setMonochrome(boolean monochrome) {
        stats.setMonochrome(monochrome);
    }

}
