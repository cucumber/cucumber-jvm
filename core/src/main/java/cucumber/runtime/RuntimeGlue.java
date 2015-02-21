package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RuntimeGlue implements Glue {
    final Map<String, StepDefinition> stepDefinitionsByPattern = new TreeMap<String, StepDefinition>();
    final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
    final List<StepHookDefinition> beforeStepHooks = new ArrayList<StepHookDefinition>();
    final List<StepHookDefinition> afterStepHooks = new ArrayList<StepHookDefinition>();

    private final UndefinedStepsTracker tracker;
    private final LocalizedXStreams localizedXStreams;

    public RuntimeGlue(UndefinedStepsTracker tracker, LocalizedXStreams localizedXStreams) {
        this.tracker = tracker;
        this.localizedXStreams = localizedXStreams;
    }

    @Override
    public void addStepDefinition(StepDefinition stepDefinition) {
        StepDefinition previous = stepDefinitionsByPattern.get(stepDefinition.getPattern());
        if (previous != null) {
            throw new DuplicateStepDefinitionException(previous, stepDefinition);
        }
        stepDefinitionsByPattern.put(stepDefinition.getPattern(), stepDefinition);
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
    public void addBeforeStepHook(StepHookDefinition hookDefinition) {
        beforeStepHooks.add(hookDefinition);
        Collections.sort(beforeHooks, new HookComparator(false));
    }

    @Override
    public void addAfterStepHook(StepHookDefinition hookDefinition) {
        afterStepHooks.add(hookDefinition);
        Collections.sort(afterStepHooks, new HookComparator(false));
    }

    @Override
    public List<StepHookDefinition> getBeforeStepHooks() {
        return beforeStepHooks;
    }

    @Override
    public List<StepHookDefinition> getAfterStepHooks() {
        return afterStepHooks;
    }

    @Override
    public StepDefinitionMatch stepDefinitionMatch(String featurePath, Step step, I18n i18n) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(featurePath, step);
        try {
            if (matches.size() == 0) {
                tracker.addUndefinedStep(step, i18n);
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                throw new AmbiguousStepDefinitionsException(matches);
            }
        } finally {
            tracker.storeStepKeyword(step, i18n);
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String featurePath, Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new StepDefinitionMatch(arguments, stepDefinition, featurePath, step, localizedXStreams));
            }
        }
        return result;
    }

    @Override
    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            stepDefinitionReporter.stepDefinition(stepDefinition);
        }
    }

    @Override
    public void removeScenarioScopedGlue() {
        removeScenarioScopedHooks(beforeHooks);
        removeScenarioScopedHooks(afterHooks);
        removeScenarioScopedStepHooks(beforeStepHooks);
        removeScenarioScopedStepHooks(afterStepHooks);
        removeScenarioScopedStepdefs();
    }

    private void removeScenarioScopedHooks(List<HookDefinition> beforeHooks1) {
        Iterator<HookDefinition> hookIterator = beforeHooks1.iterator();
        while(hookIterator.hasNext()) {
            HookDefinition hook = hookIterator.next();
            if(hook.isScenarioScoped()) {
                hookIterator.remove();
            }
        }
    }
    
    private void removeScenarioScopedStepHooks(List<StepHookDefinition> hooks) {
        Iterator<StepHookDefinition> hookIt = hooks.iterator();
        while (hookIt.hasNext()) {
            StepHookDefinition hook = hookIt.next();
            if(hook.isScenarioScoped()) {
                hookIt.remove();
            }
        }
    }

    private void removeScenarioScopedStepdefs() {
        Iterator<Map.Entry<String, StepDefinition>> stepdefs = stepDefinitionsByPattern.entrySet().iterator();
        while(stepdefs.hasNext()) {
            StepDefinition stepDefinition = stepdefs.next().getValue();
            if(stepDefinition.isScenarioScoped()) {
                stepdefs.remove();
            }
        }
    }
}
