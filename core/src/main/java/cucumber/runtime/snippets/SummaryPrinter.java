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
        printErrors(runtime);
        printSnippets(runtime);
        printExecution(runtime);
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
    
    private void printExecution(cucumber.runtime.Runtime runtime){
    	out.println(String.format("Total features: %s, Failures: %s, Skipped: %s",
    			runtime.getfeaturesRun() 
    	    	,runtime.getErrors().size(),
    	    	runtime.getSnippets().size()));
    }
}
