package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.event.StepDefinedEvent;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;

import java.util.*;

final class CachingGlue implements Glue {
    private static final Comparator<HookDefinition> ASCENDING = Comparator.comparing(HookDefinition::getOrder);
    private static final Comparator<HookDefinition> DESCENDING = ASCENDING.reversed();

    private final List<ParameterTypeDefinition> parameterTypeDefinitions = new ArrayList<>();
    private final List<DataTableTypeDefinition> dataTableTypeDefinitions = new ArrayList<>();
    private final List<DefaultParameterTransformerDefinition> defaultParameterTransformers = new ArrayList<>();
    private final List<DefaultDataTableEntryTransformerDefinition> defaultDataTableEntryTransformers = new ArrayList<>();
    private final List<DefaultDataTableCellTransformerDefinition> defaultDataTableCellTransformers = new ArrayList<>();

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

    @Override
    public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
        defaultParameterTransformers.add(defaultParameterTransformer);
    }

    @Override
    public void addDefaultDataTableEntryTransformer(DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
        defaultDataTableEntryTransformers.add(defaultDataTableEntryTransformer);
    }

    @Override
    public void addDefaultDataTableCellTransformer(DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {
        defaultDataTableCellTransformers.add(defaultDataTableCellTransformer);
    }

    Collection<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    Collection<HookDefinition> getBeforeStepHooks() {
        return beforeStepHooks;
    }

    Collection<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    Collection<HookDefinition> getAfterStepHooks() {
        return afterStepHooks;
    }

    Collection<ParameterTypeDefinition> getParameterTypeDefinitions() {
        return parameterTypeDefinitions;
    }

    Collection<DataTableTypeDefinition> getDataTableTypeDefinitions() {
        return dataTableTypeDefinitions;
    }

    Collection<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    Map<String, String> getStepPatternByStepText() {
        return stepPatternByStepText;
    }

    Map<String, CoreStepDefinition> getStepDefinitionsByPattern() {
        return stepDefinitionsByPattern;
    }

    Collection<DefaultParameterTransformerDefinition> getDefaultParameterTransformers() {
        return defaultParameterTransformers;
    }

    Collection<DefaultDataTableEntryTransformerDefinition> getDefaultDataTableEntryTransformers() {
        return defaultDataTableEntryTransformers;
    }

    Collection<DefaultDataTableCellTransformerDefinition> getDefaultDataTableCellTransformers() {
        return defaultDataTableCellTransformers;
    }

    void prepareGlue(TypeRegistry typeRegistry) throws DuplicateStepDefinitionException {
        parameterTypeDefinitions.forEach(ptd -> typeRegistry.defineParameterType(ptd.parameterType()));
        dataTableTypeDefinitions.forEach(dtd -> typeRegistry.defineDataTableType(dtd.dataTableType()));

        if (defaultParameterTransformers.size() == 1) {
            DefaultParameterTransformerDefinition definition = defaultParameterTransformers.get(0);
            ParameterByTypeTransformer transformer = definition.parameterByTypeTransformer();
            typeRegistry.setDefaultParameterTransformer(transformer);
        } else if (defaultParameterTransformers.size() > 1) {
            throw new DuplicateDefaultParameterTransformers(defaultParameterTransformers);
        }

        if (defaultDataTableEntryTransformers.size() == 1) {
            DefaultDataTableEntryTransformerDefinition definition = defaultDataTableEntryTransformers.get(0);
            TableEntryByTypeTransformer transformer = definition.tableEntryByTypeTransformer();
            typeRegistry.setDefaultDataTableEntryTransformer(transformer);
        } else if (defaultDataTableEntryTransformers.size() > 1) {
            throw new DuplicateDefaultDataTableEntryTransformers(defaultDataTableEntryTransformers);
        }


        if (defaultDataTableCellTransformers.size() == 1) {
            DefaultDataTableCellTransformerDefinition definition = defaultDataTableCellTransformers.get(0);
            TableCellByTypeTransformer transformer = definition.tableCellByTypeTransformer();
            typeRegistry.setDefaultDataTableCellTransformer(transformer);
        } else if (defaultDataTableCellTransformers.size() > 1) {
            throw new DuplicateDefaultDataTableCellTransformers(defaultDataTableCellTransformers);
        }

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
        removeScenarioScopedGlue(defaultParameterTransformers);
        removeScenarioScopedGlue(defaultDataTableEntryTransformers);
        removeScenarioScopedGlue(defaultDataTableCellTransformers);
    }

    private void removeScenarioScopedGlue(Iterable<?> glues) {
        Iterator<?> glueIterator = glues.iterator();
        while (glueIterator.hasNext()) {
            Object glue = glueIterator.next();
            if (glue instanceof ScenarioScoped) {
                ScenarioScoped scenarioScopedHookDefinition = (ScenarioScoped) glue;
                scenarioScopedHookDefinition.disposeScenarioScope();
                glueIterator.remove();
            }
        }
    }

}
