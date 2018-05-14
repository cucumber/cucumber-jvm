package cucumber.runner;

import cucumber.runtime.Glue;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.UnreportedStepExecutor;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: hmm not so sure about the Glue reference as have cloned() it since, but without some real usages I'm not sure at this point
public class DefaultUnreportedStepExecutor implements UnreportedStepExecutor {

    private final Glue glue;
    
    public DefaultUnreportedStepExecutor(final Glue glue) {
        this.glue = glue;
    }

    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    @Override
    public void runUnreportedStep(final String featurePath, final String language, 
                                  final String stepName, final int line, 
                                  final List<PickleRow> dataTableRows, final PickleString docString) throws Throwable {
        List<Argument> arguments = new ArrayList<Argument>();
        if (dataTableRows != null && !dataTableRows.isEmpty()) {
            arguments.add(new PickleTable(dataTableRows));
        } else if (docString != null) {
            arguments.add(docString);
        }
        PickleStep step = new PickleStep(stepName, arguments, Collections.<PickleLocation>emptyList());

        StepDefinitionMatch match = glue.stepDefinitionMatch(featurePath, step);
        if (match == null) {
            UndefinedStepException error = new UndefinedStepException(step);

            StackTraceElement[] originalTrace = error.getStackTrace();
            StackTraceElement[] newTrace = new StackTraceElement[originalTrace.length + 1];
            newTrace[0] = new StackTraceElement("✽", "StepDefinition", featurePath, line);
            System.arraycopy(originalTrace, 0, newTrace, 1, originalTrace.length);
            error.setStackTrace(newTrace);

            throw error;
        }
        match.runStep(language, null);
    }
}
