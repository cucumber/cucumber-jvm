package cucumber.api;

import cucumber.runtime.Runtime;

/**
 * Interface for plugins that print a summary after test execution.
 *
 * @see Plugin
 */
public interface SummaryPrinter extends Plugin {
    void print(Runtime runtime);
}
