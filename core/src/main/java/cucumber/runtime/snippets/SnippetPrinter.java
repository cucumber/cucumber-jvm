package cucumber.runtime.snippets;

import cucumber.runtime.*;
import gherkin.formatter.NiceAppendable;

import java.util.List;

public class SnippetPrinter {
    private final NiceAppendable out;

    public SnippetPrinter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    public void printSnippets(cucumber.runtime.Runtime runtime) {
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
