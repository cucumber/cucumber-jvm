package cucumber.runtime;

import cucumber.api.SummaryPrinter;
import cucumber.api.SummaryPrintingInterface;

import java.io.PrintStream;
import java.util.List;

public class DefaultSummaryPrinter implements SummaryPrinter {
    private final PrintStream out;

    public DefaultSummaryPrinter() {
        this.out = System.out;
    }

    @Override
    public void print(SummaryPrintingInterface summaryPrinting) {
        out.println();
        printStats(summaryPrinting);
        out.println();
        printErrors(summaryPrinting);
        printSnippets(summaryPrinting);
    }

    private void printStats(SummaryPrintingInterface summaryPrinting) {
        summaryPrinting.printStats(out);
    }

    private void printErrors(SummaryPrintingInterface summaryPrinting) {
        for (Throwable error : summaryPrinting.getErrors()) {
            error.printStackTrace(out);
            out.println();
        }
    }

    private void printSnippets(SummaryPrintingInterface summaryPrinting) {
        List<String> snippets = summaryPrinting.getSnippets();
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
