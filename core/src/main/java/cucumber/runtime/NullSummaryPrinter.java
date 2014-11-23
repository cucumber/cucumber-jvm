package cucumber.runtime;

import cucumber.api.SummaryPrinter;

public class NullSummaryPrinter implements SummaryPrinter {

    @Override
    public void print(Runtime runtime) {
        // Do nothing
    }

}
