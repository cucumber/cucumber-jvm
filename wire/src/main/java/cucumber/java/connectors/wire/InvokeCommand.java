package cucumber.java.connectors.wire;

import cucumber.java.exception.InvokeFailureException;
import cucumber.java.exception.PendingStepException;
import cucumber.java.CukeEngine;

import java.util.List;

public class InvokeCommand implements WireCommand {
    private String stepId;
    private List<String> args;
    private List<List<String>> tableArg;

    public InvokeCommand(String stepId, List<String> args, List<List<String>> tableArg) {
        this.stepId = stepId;
        this.args = args;
        this.tableArg = tableArg;
    }

    public WireResponse run(CukeEngine engine) {
        try {
            engine.invokeStep(stepId, args, tableArg);
            return new SuccessResponse();
        } catch (InvokeFailureException e) {
            return new FailureResponse(e.getMessage(), e);
        } catch (PendingStepException e) {
            return new PendingResponse(e.getMessage());
        } catch (Exception e) {
            return new FailureResponse(e);
        }
    }
}
