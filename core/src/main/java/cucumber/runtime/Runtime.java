package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;

import java.io.IOException;
import java.util.List;

public interface Runtime extends UnreportedStepExecutor {
    void run() throws IOException;

    void printSummary();

    List<Throwable> getErrors();

    byte exitStatus();

    List<String> getSnippets();

    Glue getGlue();

    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    @Override
    void runUnreportedStep(String featurePath, I18n i18n, String stepKeyword, String stepName, int line, List<DataTableRow> dataTableRows, DocString docString) throws Throwable;
}