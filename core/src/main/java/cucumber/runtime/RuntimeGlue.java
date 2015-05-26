package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.*;

public class RuntimeGlue implements Glue {
    final Map<String, StepDefinition> stepDefinitionsByPattern = new TreeMap<String, StepDefinition>();
    final List<HookDefinition> beforeHookDefinitions = new ArrayList<HookDefinition>();
    final List<HookDefinition> afterHookDefinitions = new ArrayList<HookDefinition>();
    final List<HookDefinition> beforeStepHookDefinitions = new ArrayList<HookDefinition>();
    final List<HookDefinition> afterStepHookDefinitions = new ArrayList<HookDefinition>();

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
        addBeforeHook(hookDefinition, HookScope.SCENARIO);
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        addAfterHook(hookDefinition, HookScope.SCENARIO);
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return getBeforeHooks(HookScope.SCENARIO);
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return getAfterHooks(HookScope.SCENARIO);
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition, HookScope scope) {
        switch (scope) {
            case STEP:
                beforeStepHookDefinitions.add(hookDefinition);
                Collections.sort(beforeStepHookDefinitions, new HookComparator(true));
                break;

            case SCENARIO:
            default:
                beforeHookDefinitions.add(hookDefinition);
                Collections.sort(beforeHookDefinitions, new HookComparator(true));
                break;
        }
        
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition, HookScope scope) {
        switch (scope) {
            case STEP:
                afterStepHookDefinitions.add(hookDefinition);
                Collections.sort(afterStepHookDefinitions, new HookComparator(false));
                break;

            case SCENARIO:
            default:
                afterHookDefinitions.add(hookDefinition);
                Collections.sort(afterHookDefinitions, new HookComparator(false));
                break;
        }
        
    }

    @Override
    public List<HookDefinition> getBeforeHooks(HookScope scope) {
        switch (scope) {
            case STEP:
                return beforeStepHookDefinitions;

            case SCENARIO:
            default:
                return beforeHookDefinitions;
        }
    }

    @Override
    public List<HookDefinition> getAfterHooks(HookScope scope) {
        switch (scope) {
            case STEP:
                return afterStepHookDefinitions;

            case SCENARIO:
            default:
                return afterHookDefinitions;
        }
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
        removeScenarioScopedHooks(beforeHookDefinitions);
        removeScenarioScopedHooks(afterHookDefinitions);
        removeScenarioScopedHooks(beforeStepHookDefinitions);
        removeScenarioScopedHooks(afterStepHookDefinitions);
        removeScenarioScopedStepdefs();
    }

    private void removeScenarioScopedHooks(List<HookDefinition> beforeHooks1) {
        Iterator<HookDefinition> hookIterator = beforeHooks1.iterator();
        while (hookIterator.hasNext()) {
            HookDefinition hookDefinition = hookIterator.next();
            if (hookDefinition.isScenarioScoped()) {
                hookIterator.remove();
            }
        }
    }

    private void removeScenarioScopedStepdefs() {
        Iterator<Map.Entry<String, StepDefinition>> stepdefs = stepDefinitionsByPattern.entrySet().iterator();
        while (stepdefs.hasNext()) {
            StepDefinition stepDefinition = stepdefs.next().getValue();
            if (stepDefinition.isScenarioScoped()) {
                stepdefs.remove();
            }
        }
    }
}