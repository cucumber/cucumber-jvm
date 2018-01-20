package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RuntimeGlue implements Glue {
    final Map<String, StepDefinition> stepDefinitionsByPattern = new TreeMap<String, StepDefinition>();
    final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
    final Map<String, CacheEntry> matchedStepDefinitionsCache = new HashMap<String, CacheEntry>();
    private final LocalizedXStreams localizedXStreams;

    public RuntimeGlue(LocalizedXStreams localizedXStreams) {
        this(null, localizedXStreams);
    }

    @Deprecated
    public RuntimeGlue(UndefinedStepsTracker tracker, LocalizedXStreams localizedXStreams) {
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
    public StepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step) {
        String stepText = step.getText();

        CacheEntry cacheEntry = matchedStepDefinitionsCache.get(stepText);
        if (cacheEntry != null) {
            return new StepDefinitionMatch(cacheEntry.arguments, cacheEntry.stepDefinition, featurePath, step, localizedXStreams);
        }

        List<StepDefinitionMatch> matches = stepDefinitionMatches(featurePath, step);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() == 1) {
            StepDefinitionMatch match = matches.get(0);
            matchedStepDefinitionsCache.put(stepText, new CacheEntry(match.getStepDefinition(), match.getArguments()));
            return match;
        }
        throw new AmbiguousStepDefinitionsException(step, matches);
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String featurePath, PickleStep step) {
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
        List<Argument> arguments;

        private CacheEntry(StepDefinition stepDefinition, List<Argument> arguments) {
            this.stepDefinition = stepDefinition;
            this.arguments = arguments;
        }
    }
}
