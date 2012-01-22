package cucumber.runtime;

import cucumber.table.DataTable;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;

import java.util.List;
import java.util.Locale;

public interface UnreportedStepExecutor {
    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    void runUnreportedStep(String uri, Locale locale, String stepKeyword, String stepName, int line, List<DataTableRow> dataTableRows, DocString docString) throws Throwable;
}
