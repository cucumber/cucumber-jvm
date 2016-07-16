package cucumber.runtime;

import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleString;

import java.util.List;

public interface UnreportedStepExecutor {
    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    void runUnreportedStep(String featurePath, String language, String stepName, int line, List<PickleRow> dataTableRows, PickleString docString) throws Throwable;
}
