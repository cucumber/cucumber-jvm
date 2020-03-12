package io.cucumber.core.runner;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.UndefinedParameterTypeException;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.StepDefinedEvent;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

final class CachingGlue implements Glue {
    private static final Comparator<CoreHookDefinition> ASCENDING = Comparator
        .comparingInt(CoreHookDefinition::getOrder)
        .thenComparing((a, b) -> {
            boolean aScenarioScoped = (a instanceof ScenarioScoped);
            boolean bScenarioScoped = (b instanceof ScenarioScoped);
            return Boolean.compare(aScenarioScoped, bScenarioScoped);
        });
    private final List<ParameterTypeDefinition> parameterTypeDefinitions = new ArrayList<>();
    private final List<DataTableTypeDefinition> dataTableTypeDefinitions = new ArrayList<>();
    private final List<DefaultParameterTransformerDefinition> defaultParameterTransformers = new ArrayList<>();
    private final List<CoreDefaultDataTableEntryTransformerDefinition> defaultDataTableEntryTransformers = new ArrayList<>();
    private final List<DefaultDataTableCellTransformerDefinition> defaultDataTableCellTransformers = new ArrayList<>();
    private final List<DocStringTypeDefinition> docStringTypeDefinitions = new ArrayList<>();

    private final List<CoreHookDefinition> beforeHooks = new ArrayList<>();
    private final List<CoreHookDefinition> beforeStepHooks = new ArrayList<>();
    private final List<StepDefinition> stepDefinitions = new ArrayList<>();
    private final List<CoreHookDefinition> afterStepHooks = new ArrayList<>();
    private final List<CoreHookDefinition> afterHooks = new ArrayList<>();

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

    public static StepExpression createExpression(List<ParameterInfo> parameterInfos, String expression, StepTypeRegistry stepTypeRegistry) {
        if (parameterInfos == null || parameterInfos.isEmpty()) {
            return new StepExpressionFactory(stepTypeRegistry).createExpression(expression);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            Supplier<Type> typeResolver = parameterInfo.getTypeResolver()::resolve;
            boolean transposed = parameterInfo.isTransposed();
            return new StepExpressionFactory(stepTypeRegistry).createExpression(expression, typeResolver, transposed);
        }
    }

    @Override
    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(CoreHookDefinition.create(hookDefinition));
        beforeHooks.sort(ASCENDING);
    }

    @Override
    public void addBeforeStepHook(HookDefinition hookDefinition) {
        beforeStepHooks.add(CoreHookDefinition.create(hookDefinition));
        beforeStepHooks.sort(ASCENDING);
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(CoreHookDefinition.create(hookDefinition));
        afterHooks.sort(ASCENDING);
    }

    @Override
    public void addAfterStepHook(HookDefinition hookDefinition) {
        afterStepHooks.add(CoreHookDefinition.create(hookDefinition));
        afterStepHooks.sort(ASCENDING);
    }

    @Override
    public void addParameterType(ParameterTypeDefinition parameterType) {
        parameterTypeDefinitions.add(parameterType);
    }

    @Override
    public void addDataTableType(DataTableTypeDefinition dataTableType) {
        dataTableTypeDefinitions.add(dataTableType);
    }

    @Override
    public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
        defaultParameterTransformers.add(defaultParameterTransformer);
    }

    @Override
    public void addDefaultDataTableEntryTransformer(DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
        defaultDataTableEntryTransformers.add(CoreDefaultDataTableEntryTransformerDefinition.create(defaultDataTableEntryTransformer));
    }

    @Override
    public void addDefaultDataTableCellTransformer(DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {
        defaultDataTableCellTransformers.add(defaultDataTableCellTransformer);
    }

    @Override
    public void addDocStringType(DocStringTypeDefinition docStringType) {
        docStringTypeDefinitions.add(docStringType);
    }

    Collection<CoreHookDefinition> getBeforeHooks() {
        return new ArrayList<>(beforeHooks);
    }

    Collection<CoreHookDefinition> getBeforeStepHooks() {
        return new ArrayList<>(beforeStepHooks);
    }

    Collection<CoreHookDefinition> getAfterHooks() {
        List<CoreHookDefinition> hooks = new ArrayList<>(afterHooks);
        Collections.reverse(hooks);
        return hooks;
    }

    Collection<CoreHookDefinition> getAfterStepHooks() {
        List<CoreHookDefinition> hooks = new ArrayList<>(afterStepHooks);
        Collections.reverse(hooks);
        return hooks;
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

    Collection<CoreDefaultDataTableEntryTransformerDefinition> getDefaultDataTableEntryTransformers() {
        return defaultDataTableEntryTransformers;
    }

    Collection<DefaultDataTableCellTransformerDefinition> getDefaultDataTableCellTransformers() {
        return defaultDataTableCellTransformers;
    }

    List<DocStringTypeDefinition> getDocStringTypeDefinitions() {
        return docStringTypeDefinitions;
    }

    void prepareGlue(StepTypeRegistry stepTypeRegistry) throws DuplicateStepDefinitionException {
        parameterTypeDefinitions.forEach(ptd -> stepTypeRegistry.defineParameterType(ptd.parameterType()));
        dataTableTypeDefinitions.forEach(dtd -> stepTypeRegistry.defineDataTableType(dtd.dataTableType()));
        docStringTypeDefinitions.forEach(dtd -> stepTypeRegistry.defineDocStringType(dtd.docStringType()));

        if (defaultParameterTransformers.size() == 1) {
            DefaultParameterTransformerDefinition definition = defaultParameterTransformers.get(0);
            ParameterByTypeTransformer transformer = definition.parameterByTypeTransformer();
            stepTypeRegistry.setDefaultParameterTransformer(transformer);
        } else if (defaultParameterTransformers.size() > 1) {
            throw new DuplicateDefaultParameterTransformers(defaultParameterTransformers);
        }

        if (defaultDataTableEntryTransformers.size() == 1) {
            DefaultDataTableEntryTransformerDefinition definition = defaultDataTableEntryTransformers.get(0);
            TableEntryByTypeTransformer transformer = definition.tableEntryByTypeTransformer();
            stepTypeRegistry.setDefaultDataTableEntryTransformer(transformer);
        } else if (defaultDataTableEntryTransformers.size() > 1) {
            throw new DuplicateDefaultDataTableEntryTransformers(defaultDataTableEntryTransformers);
        }

        if (defaultDataTableCellTransformers.size() == 1) {
            DefaultDataTableCellTransformerDefinition definition = defaultDataTableCellTransformers.get(0);
            TableCellByTypeTransformer transformer = definition.tableCellByTypeTransformer();
            stepTypeRegistry.setDefaultDataTableCellTransformer(transformer);
        } else if (defaultDataTableCellTransformers.size() > 1) {
            throw new DuplicateDefaultDataTableCellTransformers(defaultDataTableCellTransformers);
        }

        parameterTypeDefinitions.forEach(parameterTypeDefinition -> {
            ParameterType<?> parameterType = parameterTypeDefinition.parameterType();
            bus.send(
                Messages.Envelope.newBuilder()
                    .setParameterType(
                        Messages.ParameterType.newBuilder()
                            .setName(parameterType.getName())
                            .addAllRegularExpressions(parameterType.getRegexps())
                            .setPreferForRegularExpressionMatch(parameterType.preferForRegexpMatch())
                            .setUseForSnippets(parameterType.useForSnippets())
                    )
                    .build()
            );
        });

        // TODO: Redefine hooks for each scenario, similar to how we're doing for CoreStepDefinition
        beforeHooks.forEach(this::emitHook);

        stepDefinitions.forEach(stepDefinition -> {
            try {
                StepExpression stepExpression = createExpression(stepDefinition.parameterInfos(), stepDefinition.getPattern(), stepTypeRegistry);
                CoreStepDefinition coreStepDefinition = new CoreStepDefinition(bus.generateId(), stepDefinition, stepExpression);
                CoreStepDefinition previous = stepDefinitionsByPattern.get(stepDefinition.getPattern());
                if (previous != null) {
                    throw new DuplicateStepDefinitionException(previous.getStepDefinition(), stepDefinition);
                }
                stepDefinitionsByPattern.put(coreStepDefinition.getPattern(), coreStepDefinition);
                emitStepDefined(coreStepDefinition);
            } catch (UndefinedParameterTypeException e) {
                bus.send(
                    Messages.Envelope.newBuilder()
                        .setUndefinedParameterType(Messages.UndefinedParameterType.newBuilder()
                            .setExpression(stepDefinition.getPattern())
                            .setName(e.getUndefinedParameterTypeName())
                        ).build()
                );
            }
        });

        afterHooks.forEach(this::emitHook);
    }

    private void emitHook(CoreHookDefinition hook) {
        bus.send(Messages.Envelope.newBuilder()
            .setHook(Messages.Hook.newBuilder()
                .setId(hook.getId().toString())
                .setTagExpression(hook.getTagExpression())
                .setSourceReference(Messages.SourceReference.newBuilder()
                    // TODO: Maybe we should add a proper URI prefix here, like "javamethod:....". Maybe there is
                    // a standard for this
                    .setUri(hook.getLocation()))
            )
            .build()
        );
    }

    private void emitStepDefined(CoreStepDefinition stepDefinition) {
        bus.send(new StepDefinedEvent(
                bus.getInstant(),
                new io.cucumber.plugin.event.StepDefinition(
                    stepDefinition.getStepDefinition().getLocation(),
                    stepDefinition.getPattern()
                )
            )
        );

        // TODO: this is fragile - make CucumberExpression public again (but with a package-private constructor)
        boolean isCucumberExpression = stepDefinition.getExpressionClass().getName().equals("io.cucumber.cucumberexpressions.CucumberExpression");
        Messages.StepDefinitionPatternType type = isCucumberExpression ? Messages.StepDefinitionPatternType.CUCUMBER_EXPRESSION : Messages.StepDefinitionPatternType.REGULAR_EXPRESSION;
        bus.send(Messages.Envelope.newBuilder()
            .setStepDefinition(
                Messages.StepDefinition.newBuilder()
                    .setId(stepDefinition.getId().toString())
                    .setPattern(Messages.StepDefinitionPattern.newBuilder()
                        .setType(type)
                        .setSource(stepDefinition.getPattern()))
                    .setSourceReference(Messages.SourceReference.newBuilder()
                        .setUri(stepDefinition.getStepDefinition().getLocation()))
                    .build())
            .build()
        );
    }

    PickleStepDefinitionMatch stepDefinitionMatch(URI uri, Step step) throws AmbiguousStepDefinitionsException {
        PickleStepDefinitionMatch cachedMatch = cachedStepDefinitionMatch(uri, step);
        if (cachedMatch != null) {
            return cachedMatch;
        }
        return findStepDefinitionMatch(uri, step);
    }


    private PickleStepDefinitionMatch cachedStepDefinitionMatch(URI uri, Step step) {
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
        return new PickleStepDefinitionMatch(arguments, coreStepDefinition.getStepDefinition(), uri, step);
    }

    private PickleStepDefinitionMatch findStepDefinitionMatch(URI uri, Step step) throws AmbiguousStepDefinitionsException {
        List<PickleStepDefinitionMatch> matches = stepDefinitionMatches(uri, step);
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

    private List<PickleStepDefinitionMatch> stepDefinitionMatches(URI uri, Step step) {
        List<PickleStepDefinitionMatch> result = new ArrayList<>();
        for (CoreStepDefinition coreStepDefinition : stepDefinitionsByPattern.values()) {
            List<Argument> arguments = coreStepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new PickleStepDefinitionMatch(arguments, coreStepDefinition.getStepDefinition(), uri, step));
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
        removeScenarioScopedGlue(docStringTypeDefinitions);
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
                glueIterator.remove();
            }
        }
    }

}
