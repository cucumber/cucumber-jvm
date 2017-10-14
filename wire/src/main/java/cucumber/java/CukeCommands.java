package cucumber.java;

import cucumber.api.Scenario;
import cucumber.java.hook.HookRegistrar;
import cucumber.java.step.InvokeArgs;
import cucumber.java.step.InvokeResult;
import cucumber.java.step.StepInfo;
import cucumber.java.step.StepManager;
import cucumber.runtime.ScenarioImpl;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.Set;

public class CukeCommands {
    private static Scenario currentScenario;

    private StepManager stepManager = new StepManager();

    public StepManager getStepManager() {
        return stepManager;
    }

    public void beginScenario(Set<Tag> tags) throws Throwable {
        gherkin.formatter.model.Scenario gherkinScenario = new gherkin.formatter.model.Scenario(null, null, null, null, null, null, null);
        currentScenario = new ScenarioImpl(null, tags, gherkinScenario);
        HookRegistrar.execBeforeHooks(currentScenario);
    }

    public void endScenario() throws Throwable {
        HookRegistrar.execAfterHooks(currentScenario);
        currentScenario = null;
    }

    public String snippetText(String stepKeyword, String stepName) {
        // TODO: output snippet text
        return "<<<snippet text>>>";
    }

    public MatchResult stepMatches(String description) {
        return stepManager.stepMatches(description);
    }

    public InvokeResult invoke(int id, InvokeArgs args) throws Throwable {
        StepInfo stepInfo = stepManager.getStep(id);
        InvokeResult result = HookRegistrar.execStepChain(currentScenario, stepInfo, args);
        HookRegistrar.execAfterStepHooks(currentScenario);
        return result;
    }
}
