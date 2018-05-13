package cucumber.api;

import java.io.PrintStream;
import java.util.List;

/**
 * Provides methods for printing the statistics, errors and snippets to
 * a SummaryPrinter plugin.
 *
 */
public interface SummaryPrintingInterface {
    /**
     * Prints the statistics to the PrintStream argument.
     * @param out The PrintStream to print to.
     */
    void printStats(PrintStream out);

    /**
     * Returns the errors that occurred during the execution of the test cases
     * from the feature files.
     * @return the errors.
     */
    List<Throwable> getErrors();

    /**
     * Returns the snippets created for the undefined steps encountered during
     * the execution of the test cases from the feature files.
     * @return the snippets.
     */
    List<String> getSnippets();
}
