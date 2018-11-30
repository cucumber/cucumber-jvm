package cucumber.runtime.formatter;

import cucumber.api.SummaryPrinter;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.StrictAware;
import cucumber.runtime.UndefinedStepsTracker;

import java.io.PrintStream;
import java.util.List;

class DefaultSummaryPrinter implements SummaryPrinter, ColorAware, StrictAware, EventListener {

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
