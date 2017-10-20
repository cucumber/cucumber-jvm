package cucumber.api;

import cucumber.runtime.Runtime;

public interface SummaryPrinter extends Plugin {
    void print(Runtime runtime);
}
