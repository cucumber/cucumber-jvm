package cucumber.java;

import cucumber.java.exception.CukeException;
import cucumber.java.exception.InvokeException;
import cucumber.java.exception.InvokeFailureException;
import cucumber.java.exception.PendingStepException;
import cucumber.java.step.InvokeArgs;
import cucumber.java.step.InvokeResult;
import cucumber.java.step.SingleStepMatch;
import cucumber.java.utils.RegexSubmatch;
import gherkin.formatter.model.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CukeEngineImpl implements CukeEngine {
    private CukeCommands cukeCommands = new CukeCommands();

    public ArrayList<StepMatch> stepMatches(String name) {
        ArrayList<StepMatch> engineResult = new ArrayList<StepMatch>();
        MatchResult commandResult = cukeCommands.stepMatches(name);
        for (SingleStepMatch commandMatch : commandResult.getResultSet()) {
            ArrayList<StepMatchArg> args = new ArrayList<StepMatchArg>();
            for (RegexSubmatch commandMatchArg : commandMatch.submatches) {
                StepMatchArg engineMatchArg = new StepMatchArg(commandMatchArg.value, commandMatchArg.position);
                args.add(engineMatchArg);
            }

            StepMatch engineMatch = new StepMatch(
                    String.valueOf(commandMatch.stepInfo.id),
                    args,
                    commandMatch.stepInfo.source,
                    commandMatch.stepInfo.regex.toString());

            engineResult.add(engineMatch);
        }
        return engineResult;
    }

    public void beginScenario(List<String> tagList) throws Throwable {
        Set<Tag> tagSet = new HashSet<Tag>();
        if (tagList != null) {
            for (String tagName : tagList) {
                tagSet.add(new Tag(tagName, null));
            }
        }
        cukeCommands.beginScenario(tagSet);
    }

    public void invokeStep(String id, List<String> args, List<List<String>> tableArg)
            throws InvokeException, PendingStepException, InvokeFailureException {
        InvokeArgs commandArgs = new InvokeArgs();
        try {
            if (args != null) {
                for (String a : args) {
                    commandArgs.addArg(a);
                }
            }

            if (tableArg != null) {
                int numRows = tableArg.size();
                int numCols = (numRows > 0 ? tableArg.get(0).size() : 0);

                if (numRows > 0 && numCols > 0) {
                    Table commandTableArg = commandArgs.getTableArg();

                    List<String> row0 = tableArg.get(0);
                    for (int col = 0; col < numCols; ++col) {
                        commandTableArg.addColumn(row0.get(col));
                    }

                    for (int row = 1; row < numRows; ++row) {
                        commandTableArg.addRow(tableArg.get(row));
                    }
                }
            }
        } catch (CukeException e) {
            throw new InvokeException("Unable to decode arguments");
        }

        InvokeResult commandResult;
        try {
            commandResult = cukeCommands.invoke(Integer.parseInt(id), commandArgs);
        } catch (Throwable e) {
            throw new InvokeException("Uncaught exception");
        }

        switch (commandResult.getType()) {
            case SUCCESS:
                return;
            case FAILURE:
                throw new InvokeFailureException(commandResult.getDescription());
            case PENDING:
                throw new PendingStepException(commandResult.getDescription());
        }
    }

    public void endScenario(List<String> tags) throws Throwable {
        cukeCommands.endScenario();
    }

    public String snippetText(String keyword, String name, String multilineArgClass) {
        return cukeCommands.snippetText(keyword, name);
    }
}
