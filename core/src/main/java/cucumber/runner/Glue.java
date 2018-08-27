package cucumber.runner;

import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import io.cucumber.stepexpression.Argument;
import cucumber.api.StepDefinitionReporter;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class Glue implements cucumber.runtime.Glue {
    final Map<String, StepDefinition> stepDefinitionsByPattern = new TreeMap<>();
    final List<HookDefinition> beforeHooks = new ArrayList<>();
    final List<HookDefinition> beforeStepHooks = new ArrayList<>();
    final List<HookDefinition> afterHooks = new ArrayList<>();
    final List<HookDefinition> afterStepHooks = new ArrayList<>();
    final Map<String, CacheEntry> matchedStepDefinitionsCache = new HashMap<>();

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
    public void addBeforeStepHook(HookDefinition hookDefinition) {
        beforeStepHooks.add(hookDefinition);
        Collections.sort(beforeStepHooks, new HookComparator(true));
    }
    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
        Collections.sort(afterHooks, new HookComparator(false));
    }

    @Override
    public void addAfterStepHook(HookDefinition hookDefinition) {
        afterStepHooks.add(hookDefinition);
        Collections.sort(afterStepHooks, new HookComparator(false));
    }

    List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    List<HookDefinition> getBeforeStepHooks() {
        return beforeStepHooks;
    }

    List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    List<HookDefinition> getAfterStepHooks() {
        return afterStepHooks;
    }

    PickleStepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step) {
        String stepText = step.getText();

        CacheEntry cacheEntry = matchedStepDefinitionsCache.get(stepText);
        if (cacheEntry != null) {
            return new PickleStepDefinitionMatch(Collections.<Argument>emptyList(), cacheEntry.stepDefinition, featurePath, step);
        }

        List<PickleStepDefinitionMatch> matches = stepDefinitionMatches(featurePath, step);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new AmbiguousStepDefinitionsException(step, matches);
        }

        PickleStepDefinitionMatch match = matches.get(0);

        // We can only cache step definitions without arguments.
        // DocString and TableArguments are not included in the stepText used as the cache key.
        if(match.getArguments().isEmpty()) {
            matchedStepDefinitionsCache.put(stepText, new CacheEntry(match.getStepDefinition()));
        }

        return match;
    }

    private List<PickleStepDefinitionMatch> stepDefinitionMatches(String featurePath, PickleStep step) {
        List<PickleStepDefinitionMatch> result = new ArrayList<PickleStepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new PickleStepDefinitionMatch(arguments, stepDefinition, featurePath, step));
            }
        }
        return result;
    }

    void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            stepDefinitionReporter.stepDefinition(stepDefinition);
        }
    }

    @Override
    public void removeScenarioScopedGlue() {
        removeScenarioScopedHooks(beforeHooks);
        removeScenarioScopedHooks(beforeStepHooks);
        removeScenarioScopedHooks(afterHooks);
        removeScenarioScopedHooks(afterStepHooks);
        removeScenarioScopedStepdefs();
    }

    private void removeScenarioScopedHooks(List<HookDefinition> beforeHooks1) {
        Iterator<HookDefinition> hookIterator = beforeHooks1.iterator();
        while (hookIterator.hasNext()) {
            HookDefinition hook = hookIterator.next();
            if (hook.isScenarioScoped()) {
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

        Iterator<Map.Entry<String, CacheEntry>> cachedStepDefs = matchedStepDefinitionsCache.entrySet().iterator();
        while(cachedStepDefs.hasNext()){
            StepDefinition stepDefinition = cachedStepDefs.next().getValue().stepDefinition;
            if(stepDefinition.isScenarioScoped()){
                cachedStepDefs.remove();
            }
        }
    }

    static final class CacheEntry {

        StepDefinition stepDefinition;

        private CacheEntry(StepDefinition stepDefinition) {
            this.stepDefinition = stepDefinition;
        }
    }
}
