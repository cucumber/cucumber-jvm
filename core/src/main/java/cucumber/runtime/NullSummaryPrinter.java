package cucumber.runtime;

import cucumber.api.SummaryPrinter;
import cucumber.api.SummaryPrintingInterface;

public class NullSummaryPrinter implements SummaryPrinter {

    @Override
    public void print(SummaryPrintingInterface summaryPrinting) {
        // Do nothing
    }

}
