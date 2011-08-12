package cucumber.runtime;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class SummaryReporter {
    private final PrintWriter out;

    public SummaryReporter(Writer out) {
        this.out = new PrintWriter(out, true);
    }

    public void snippets(List<String> snippets) {
        if (!snippets.isEmpty()) {
            out.println();
            out.println("You can implement step definitions for undefined steps with these snippets:");
            out.println();
            for (String snippet : snippets) {
                out.println(snippet);
            }
        }
    }
}
