package cucumber.api;

/**
 * Interface for plugins that print a summary after test execution.
 *
 * @see Plugin
 */
public interface SummaryPrinter extends Plugin {
    /**
     * Handles the printing of the summary to the console
     * @param summaryPrinting Provides method to print the stats, errors and snippets
     */
    void print(SummaryPrintingInterface summaryPrinting);
}
