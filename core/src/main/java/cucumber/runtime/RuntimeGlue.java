package cucumber.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuntimeGlue implements Glue {
    /*
    In the light of how this class is now used - one instance - purpose is to store glue code - I think we should rename it to RuntimeGlue implements Glue.
    I think it should just be Glue, there's no execution in it anymore
     */
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();


    @Override
    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(hookDefinition);
        Collections.sort(beforeHooks, new HookComparator(true));
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
        Collections.sort(afterHooks, new HookComparator(false));
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    @Override
    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }
}
