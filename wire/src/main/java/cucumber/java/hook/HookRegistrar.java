package cucumber.java.hook;

import cucumber.api.Scenario;
import cucumber.java.step.InvokeArgs;
import cucumber.java.step.InvokeResult;
import cucumber.java.step.StepInfo;
import cucumber.runtime.HookDefinition;

import java.util.ArrayList;
import java.util.List;

public class HookRegistrar {
    private static List<Hook> beforeHooks = new ArrayList<Hook>();
    private static List<Hook> afterHooks = new ArrayList<Hook>();
    private static List<AroundStepHook> aroundStepHooks = new ArrayList<AroundStepHook>();

    public static void execBeforeHooks(Scenario scenario) throws Throwable {
        execHooks(beforeHooks, scenario);
    }

    public static void execAfterHooks(Scenario scenario) throws Throwable {
        execHooks(afterHooks, scenario);
    }

    private static void execHooks(List<Hook> hookList, Scenario scenario) throws Throwable {
        for (Hook hook : hookList) {
            hook.invokeHook(scenario);
        }
    }

    public static InvokeResult execStepChain(Scenario scenario, StepInfo stepInfo, InvokeArgs args) throws Throwable {
        StepCallChain scc = new StepCallChain(scenario, stepInfo, args, aroundStepHooks);
        return scc.exec();
    }

    public static void execAfterStepHooks(Scenario scenaro) {

    }

    public static void registerBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(new BeforeHook(hookDefinition));
    }

    public static void registerAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(0, new AfterHook(hookDefinition));
    }

    public static void registerAroundStepHook(HookDefinition hookDefinition) {
        aroundStepHooks.add(0, new AroundStepHook(hookDefinition));
    }
}
