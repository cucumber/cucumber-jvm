package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;

import java.util.List;

public interface UnreportedStepExecutor {
    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    void runUnreportedStep(String uri, I18n i18n, String stepKeyword, String stepName, int line, List<DataTableRow> dataTableRows, DocString docString) throws Throwable;
}
