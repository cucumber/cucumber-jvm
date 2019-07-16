package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.event.StepDefinedEvent;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class CachingGlue implements Glue {
    private static final HookComparator ASCENDING = new HookComparator(true);
    private static final HookComparator DESCENDING = new HookComparator(false);

    private final List<ParameterTypeDefinition> parameterTypeDefinitions = new ArrayList<>();
    private final List<DataTableTypeDefinition> dataTableTypeDefinitions = new ArrayList<>();

    private final List<HookDefinition> beforeHooks = new ArrayList<>();
    private final List<HookDefinition> beforeStepHooks = new ArrayList<>();
    private final List<StepDefinition> stepDefinitions = new ArrayList<>();
    private final List<HookDefinition> afterStepHooks = new ArrayList<>();
    private final List<HookDefinition> afterHooks = new ArrayList<>();

    /*
     * Storing the pattern that matches the step text allows us to cache the rather slow
     * regex comparisons in `stepDefinitionMatches`.
     * This cache does not need to be cleaned. The matching pattern be will used to look
     * up a pickle specific step definition from `stepDefinitionsByPattern`.
     */
    private final Map<String, String> stepPatternByStepText = new HashMap<>();
    private final Map<String, CoreStepDefinition> stepDefinitionsByPattern = new TreeMap<>();

    private final EventBus bus;

    CachingGlue(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(hookDefinition);
        beforeHooks.sort(ASCENDING);
    }

    @Override
    public void addBeforeStepHook(HookDefinition hookDefinition) {
        beforeStepHooks.add(hookDefinition);
        beforeStepHooks.sort(ASCENDING);
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
        afterHooks.sort(DESCENDING);
    }

    @Override
    public void addAfterStepHook(HookDefinition hookDefinition) {
        afterStepHooks.add(hookDefinition);
        afterStepHooks.sort(DESCENDING);
    }

    @Override
    public void addParameterType(ParameterTypeDefinition parameterTypeDefinition) {
        parameterTypeDefinitions.add(parameterTypeDefinition);
    }

    @Override
    public void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition) {
        dataTableTypeDefinitions.add(dataTableTypeDefinition);
    }

    List<HookDefinition> getBeforeHooks() {
        return new ArrayList<>(beforeHooks);
    }

    List<HookDefinition> getBeforeStepHooks() {
        return new ArrayList<>(beforeStepHooks);
    }

    List<HookDefinition> getAfterHooks() {
        return new ArrayList<>(afterHooks);
    }

    List<HookDefinition> getAfterStepHooks() {
        return new ArrayList<>(afterStepHooks);
    }

    List<ParameterTypeDefinition> getParameterTypeDefinitions() {
        return parameterTypeDefinitions;
    }

    List<DataTableTypeDefinition> getDataTableTypeDefinitions() {
        return dataTableTypeDefinitions;
    }

    List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    Map<String, String> getStepPatternByStepText() {
        return stepPatternByStepText;
    }

    Map<String, CoreStepDefinition> getStepDefinitionsByPattern() {
        return stepDefinitionsByPattern;
    }

    void prepareGlue(TypeRegistry typeRegistry) throws DuplicateStepDefinitionException {
        parameterTypeDefinitions.forEach(ptd -> typeRegistry.defineParameterType(ptd.parameterType()));
        dataTableTypeDefinitions.forEach(dtd -> typeRegistry.defineDataTableType(dtd.dataTableType()));

        stepDefinitions.forEach(stepDefinition -> {
            CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, typeRegistry);
            CoreStepDefinition previous = stepDefinitionsByPattern.get(stepDefinition.getPattern());
            if (previous != null) {
                throw new DuplicateStepDefinitionException(previous.getStepDefinition(), stepDefinition);
            }
            stepDefinitionsByPattern.put(coreStepDefinition.getPattern(), coreStepDefinition);
            bus.send(new StepDefinedEvent(bus.getInstant(), stepDefinition));
        });
    }

    PickleStepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step) {
        PickleStepDefinitionMatch cachedMatch = cachedStepDefinitionMatch(featurePath, step);
        if (cachedMatch != null) {
            return cachedMatch;
        }
        return findStepDefinitionMatch(featurePath, step);
    }


    private PickleStepDefinitionMatch cachedStepDefinitionMatch(String featurePath, PickleStep step) {
        String stepDefinitionPattern = stepPatternByStepText.get(step.getText());
        if (stepDefinitionPattern == null) {
            return null;
        }

        CoreStepDefinition coreStepDefinition = stepDefinitionsByPattern.get(stepDefinitionPattern);
        if (coreStepDefinition == null) {
            return null;
        }

        // Step definition arguments consists of parameters included in the step text and
        // gherkin step arguments (doc string and data table) which are not included in
        // the step text. As such the step definition arguments can not be cached and
        // must be recreated each time.
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        return new PickleStepDefinitionMatch(arguments, coreStepDefinition.getStepDefinition(), featurePath, step);
    }

    private PickleStepDefinitionMatch findStepDefinitionMatch(String featurePath, PickleStep step) {
        List<PickleStepDefinitionMatch> matches = stepDefinitionMatches(featurePath, step);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new AmbiguousStepDefinitionsException(step, matches);
        }

        PickleStepDefinitionMatch match = matches.get(0);

        stepPatternByStepText.put(step.getText(), match.getPattern());

        return match;
    }

    private List<PickleStepDefinitionMatch> stepDefinitionMatches(String featurePath, PickleStep step) {
        List<PickleStepDefinitionMatch> result = new ArrayList<>();
        for (CoreStepDefinition coreStepDefinition : stepDefinitionsByPattern.values()) {
            List<Argument> arguments = coreStepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new PickleStepDefinitionMatch(arguments, coreStepDefinition.getStepDefinition(), featurePath, step));
            }
        }
        return result;
    }

    void removeScenarioScopedGlue() {
        stepDefinitionsByPattern.clear();
        removeScenarioScopedGlue(beforeHooks);
        removeScenarioScopedGlue(beforeStepHooks);
        removeScenarioScopedGlue(afterHooks);
        removeScenarioScopedGlue(afterStepHooks);
        removeScenarioScopedGlue(stepDefinitions);
        removeScenarioScopedGlue(dataTableTypeDefinitions);
        removeScenarioScopedGlue(parameterTypeDefinitions);
    }

    private void removeScenarioScopedGlue(List<?> glue) {
        Iterator<?> hookIterator = glue.iterator();
        while (hookIterator.hasNext()) {
            Object hook = hookIterator.next();
            if (hook instanceof ScenarioScoped) {
                ScenarioScoped scenarioScopedHookDefinition = (ScenarioScoped) hook;
                scenarioScopedHookDefinition.disposeScenarioScope();
                hookIterator.remove();
            }
        }
    }

}
