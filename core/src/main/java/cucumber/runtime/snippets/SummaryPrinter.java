package cucumber.runtime.snippets;

import java.io.PrintStream;
import java.util.List;

public class SummaryPrinter {
    private final PrintStream out;

    public SummaryPrinter(PrintStream out) {
        this.out = out;
    }

    public void print(cucumber.runtime.Runtime runtime) {
        out.println();
        printSummary(runtime);
        out.println();
        printErrors(runtime);
        printSnippets(runtime);
    }

    private void printSummary(cucumber.runtime.Runtime runtime) {
        runtime.printSummary(out);
    }

    private void printErrors(cucumber.runtime.Runtime runtime) {
        for (Throwable error : runtime.getErrors()) {
            error.printStackTrace(out);
            out.println();
        }
    }

    private void printSnippets(cucumber.runtime.Runtime runtime) {
        List<String> snippets = runtime.getSnippets();
        if (!snippets.isEmpty()) {
            out.append("\n");
            out.println("You can implement missing steps with the snippets below:");
            out.println();
            for (String snippet : snippets) {
                out.println(snippet);
            }
        }
    }
}
