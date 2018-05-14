package cucumber.runtime;

import cucumber.api.Argument;
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
    final Map<String, StepDefinition> stepDefinitionsByPattern;
    final List<HookDefinition> beforeHooks;
    final List<HookDefinition> beforeStepHooks;
    final List<HookDefinition> afterHooks;
    final List<HookDefinition> afterStepHooks;
    final Map<String, CacheEntry> matchedStepDefinitionsCache;
    private final LocalizedXStreams localizedXStreams;

    public RuntimeGlue(LocalizedXStreams localizedXStreams) {
        this(localizedXStreams,
            Collections.<String, StepDefinition>emptyMap(),
            Collections.<HookDefinition>emptyList(),
            Collections.<HookDefinition>emptyList(),
            Collections.<HookDefinition>emptyList(),
            Collections.<HookDefinition>emptyList(), 
            Collections.<String, CacheEntry>emptyMap());
    }

    @Deprecated
    public RuntimeGlue(UndefinedStepsTracker tracker, LocalizedXStreams localizedXStreams) {
        this(localizedXStreams);
    }

    protected RuntimeGlue(RuntimeGlue other) {
        this(other.localizedXStreams, other.stepDefinitionsByPattern, other.beforeHooks, other.beforeStepHooks, other.afterHooks, other.afterStepHooks, other.matchedStepDefinitionsCache);

    }

    private RuntimeGlue(LocalizedXStreams localizedXStreams,
                        Map<String, StepDefinition> stepDefinitionsByPattern,
                        List<HookDefinition> beforeHooks,
                        List<HookDefinition> beforeStepHooks,
                        List<HookDefinition> afterHooks,
                        List<HookDefinition> afterStepHooks,
                        Map<String, CacheEntry> matchedStepDefinitionsCache) {
        this.localizedXStreams = localizedXStreams;
        this.stepDefinitionsByPattern = new TreeMap<String, StepDefinition>(stepDefinitionsByPattern);
        this.beforeHooks = new ArrayList<HookDefinition>(beforeHooks);
        this.beforeStepHooks = new ArrayList<HookDefinition>(beforeStepHooks);
        this.afterHooks = new ArrayList<HookDefinition>(afterHooks);
        this.afterStepHooks = new ArrayList<HookDefinition>(afterStepHooks);
        this.matchedStepDefinitionsCache = new HashMap<String, CacheEntry>(matchedStepDefinitionsCache);
    }

    @Override
    public Glue clone() {
        return new RuntimeGlue(this);
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

    @Override
    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            stepDefinitionReporter.stepDefinition(stepDefinition);
        }
    }
    @Override
    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    @Override
    public List<HookDefinition> getBeforeStepHooks() {
        return beforeStepHooks;
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    @Override
    public List<HookDefinition> getAfterStepHooks() {
        return afterStepHooks;
    }

    @Override
    public PickleStepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step) {
        String stepText = step.getText();

        CacheEntry cacheEntry = matchedStepDefinitionsCache.get(stepText);
        if (cacheEntry != null) {
            return new PickleStepDefinitionMatch(cacheEntry.arguments, cacheEntry.stepDefinition, featurePath, step, localizedXStreams);
        }

        List<PickleStepDefinitionMatch> matches = stepDefinitionMatches(featurePath, step);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() == 1) {
            PickleStepDefinitionMatch match = matches.get(0);
            matchedStepDefinitionsCache.put(stepText, new CacheEntry(match.getStepDefinition(), match.getArguments()));
            return match;
        }
        throw new AmbiguousStepDefinitionsException(step, matches);
    }

    private List<PickleStepDefinitionMatch> stepDefinitionMatches(String featurePath, PickleStep step) {
        List<PickleStepDefinitionMatch> result = new ArrayList<PickleStepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new PickleStepDefinitionMatch(arguments, stepDefinition, featurePath, step, localizedXStreams));
            }
        }
        return result;
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
        List<Argument> arguments;

        private CacheEntry(StepDefinition stepDefinition, List<Argument> arguments) {
            this.stepDefinition = stepDefinition;
            this.arguments = arguments;
        }
    }
}
