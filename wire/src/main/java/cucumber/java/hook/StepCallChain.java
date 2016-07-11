package cucumber.java.hook;

import cucumber.api.Scenario;
import cucumber.java.step.InvokeArgs;
import cucumber.java.step.InvokeResult;
import cucumber.java.step.StepInfo;

import java.util.Iterator;
import java.util.List;

public class StepCallChain {
    private Scenario scenario;
    private StepInfo stepInfo;
    private InvokeArgs pStepArgs;

    private Iterator<AroundStepHook> nextHook;
    private InvokeResult result = new InvokeResult();

    public StepCallChain(Scenario scenario, StepInfo stepInfo, InvokeArgs pStepArgs, List<AroundStepHook> aroundHooks) {
        this.scenario = scenario;
        this.stepInfo = stepInfo;
        this.pStepArgs = pStepArgs;
        nextHook = aroundHooks.iterator();
    }

    public InvokeResult exec() throws Throwable {
        execNext();
        return result;
    }

    public void execNext() throws Throwable {
        if (!nextHook.hasNext()) {
            execStep();
        } else {
            AroundStepHook currentHook = nextHook.next();
            CallableStepChain callableStepChain = new CallableStepChain(this);
            currentHook.invokeHook(scenario, callableStepChain);
        }
    }

    private void execStep() {
        if (stepInfo != null) {
            result = stepInfo.invokeStep(pStepArgs);
        }
    }
}
