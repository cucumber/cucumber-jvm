package io.cucumber.core.plugin;

import io.cucumber.core.api.plugin.SummaryPrinter;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.plugin.ColorAware;
import io.cucumber.core.api.plugin.StrictAware;

import java.io.PrintStream;
import java.util.List;

public final class DefaultSummaryPrinter implements SummaryPrinter, ColorAware, StrictAware, EventListener {

    private final Stats stats = new Stats();
    private final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();

    private final PrintStream out;

    public DefaultSummaryPrinter() {
        this.out = System.out;
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
        List<String> snippets = undefinedStepsTracker.getSnippets();
        if (!snippets.isEmpty()) {
            out.append("\n");
            out.println("You can implement missing steps with the snippets below:");
            out.println();
            for (String snippet : snippets) {
                out.println(snippet);
            }
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        stats.setEventPublisher(publisher);
        undefinedStepsTracker.setEventPublisher(publisher);
        publisher.registerHandlerFor(TestRunFinished.class, new EventHandler<TestRunFinished>() {
            @Override
            public void receive(TestRunFinished event) {
                print();
            }
        });
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
