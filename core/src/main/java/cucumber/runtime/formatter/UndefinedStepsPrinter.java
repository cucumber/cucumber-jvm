package cucumber.runtime.formatter;

import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.runtime.UndefinedStepsTracker;

import java.io.PrintStream;
import java.util.List;

class UndefinedStepsPrinter implements EventListener {

    private final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();

    private final PrintStream out;

    public UndefinedStepsPrinter() {
        this.out = System.out;
    }

    private void print() {
        printSnippets();
    }

    private void printSnippets() {
        List<String> snippets = undefinedStepsTracker.getSnippets();
        if (!snippets.isEmpty()) {
            out.append("\n");
            out.println("There were undefined steps. You can implement missing steps with the snippets below:");
            out.println();
            for (String snippet : snippets) {
                out.println(snippet);
            }
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        undefinedStepsTracker.setEventPublisher(publisher);
        publisher.registerHandlerFor(TestRunFinished.class, new EventHandler<TestRunFinished>() {
            @Override
            public void receive(TestRunFinished event) {
                print();
            }
        });
    }
}
