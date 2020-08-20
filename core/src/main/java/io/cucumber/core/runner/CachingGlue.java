package io.cucumber.core.runner;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.JavaMethodReference;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.StackTraceElementReference;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.CucumberExpression;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.RegularExpression;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.Hook;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.SourceReference;
import io.cucumber.messages.Messages.StepDefinition.Builder;
import io.cucumber.messages.Messages.StepDefinition.StepDefinitionPattern;
import io.cucumber.messages.Messages.StepDefinition.StepDefinitionPattern.StepDefinitionPatternType;
import io.cucumber.plugin.event.StepDefinedEvent;

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

final class CachingGlue implements Glue {

    private static final Comparator<CoreHookDefinition> ASCENDING = Comparator
            .comparingInt(CoreHookDefinition::getOrder)
            .thenComparing(ScenarioScoped.class::isInstance);
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
     * Storing the pattern that matches the step text allows us to cache the
     * rather slow regex comparisons in `stepDefinitionMatches`. This cache does
     * not need to be cleaned. The matching pattern be will used to look up a
     * pickle specific step definition from `stepDefinitionsByPattern`.
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
        beforeHooks.add(CoreHookDefinition.create(hookDefinition));
        beforeHooks.sort(ASCENDING);
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(CoreHookDefinition.create(hookDefinition));
        afterHooks.sort(ASCENDING);
    }

    @Override
    public void addBeforeStepHook(HookDefinition hookDefinition) {
        beforeStepHooks.add(CoreHookDefinition.create(hookDefinition));
        beforeStepHooks.sort(ASCENDING);
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
    public void addDefaultDataTableEntryTransformer(
            DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
    ) {
        defaultDataTableEntryTransformers
                .add(CoreDefaultDataTableEntryTransformerDefinition.create(defaultDataTableEntryTransformer));
    }

    @Override
    public void addDefaultDataTableCellTransformer(
            DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
    ) {
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
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(stepTypeRegistry, bus);

        // TODO: separate prepared and unprepared glue into different classes
        parameterTypeDefinitions.forEach(ptd -> {
            ParameterType<?> parameterType = ptd.parameterType();
            stepTypeRegistry.defineParameterType(parameterType);
            emitParameterTypeDefined(parameterType);
        });
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

        // TODO: Redefine hooks for each scenario, similar to how we're doing
        // for CoreStepDefinition
        beforeHooks.forEach(this::emitHook);
        beforeStepHooks.forEach(this::emitHook);

        stepDefinitions.forEach(stepDefinition -> {
            StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
            CoreStepDefinition coreStepDefinition = new CoreStepDefinition(bus.generateId(), stepDefinition,
                expression);
            CoreStepDefinition previous = stepDefinitionsByPattern.get(stepDefinition.getPattern());
            if (previous != null) {
                throw new DuplicateStepDefinitionException(previous, stepDefinition);
            }
            stepDefinitionsByPattern.put(coreStepDefinition.getExpression().getSource(), coreStepDefinition);
            emitStepDefined(coreStepDefinition);
        });

        afterStepHooks.forEach(this::emitHook);
        afterHooks.forEach(this::emitHook);
    }

    private void emitParameterTypeDefined(ParameterType<?> parameterType) {
        bus.send(Messages.Envelope.newBuilder()
                .setParameterType(Messages.ParameterType.newBuilder()
                        .setId(bus.generateId().toString())
                        .setName(parameterType.getName())
                        .addAllRegularExpressions(parameterType.getRegexps())
                        .setPreferForRegularExpressionMatch(parameterType.preferForRegexpMatch())
                        .setUseForSnippets(parameterType.useForSnippets()))
                .build());
    }

    private void emitHook(CoreHookDefinition hook) {
        Hook.Builder hookDefinitionBuilder = Hook.newBuilder()
                .setId(hook.getId().toString())
                .setTagExpression(hook.getTagExpression());
        hook.getDefinitionLocation()
                .ifPresent(reference -> hookDefinitionBuilder.setSourceReference(createSourceReference(reference)));
        bus.send(Messages.Envelope.newBuilder()
                .setHook(hookDefinitionBuilder)
                .build());
    }

    private void emitStepDefined(CoreStepDefinition stepDefinition) {
        bus.send(new StepDefinedEvent(
            bus.getInstant(),
            new io.cucumber.plugin.event.StepDefinition(
                stepDefinition.getStepDefinition().getLocation(),
                stepDefinition.getExpression().getSource())));

        Builder stepDefinitionBuilder = Messages.StepDefinition.newBuilder()
                .setId(stepDefinition.getId().toString())
                .setPattern(StepDefinitionPattern.newBuilder()
                        .setSource(stepDefinition.getExpression().getSource())
                        .setType(getExpressionType(stepDefinition)));
        stepDefinition.getDefinitionLocation()
                .ifPresent(reference -> stepDefinitionBuilder.setSourceReference(createSourceReference(reference)));
        bus.send(Envelope.newBuilder()
                .setStepDefinition(stepDefinitionBuilder)
                .build());
    }

    private SourceReference.Builder createSourceReference(io.cucumber.core.backend.SourceReference reference) {
        SourceReference.Builder sourceReferenceBuilder = SourceReference.newBuilder();
        if (reference instanceof JavaMethodReference) {
            JavaMethodReference methodReference = (JavaMethodReference) reference;
            sourceReferenceBuilder.setJavaMethod(SourceReference.JavaMethod.newBuilder()
                    .setClassName(methodReference.className())
                    .setMethodName(methodReference.methodName())
                    .addAllMethodParameterTypes(methodReference.methodParameterTypes()));
        }

        if (reference instanceof StackTraceElementReference) {
            StackTraceElementReference stackReference = (StackTraceElementReference) reference;
            SourceReference.JavaStackTraceElement.Builder stackTraceElementBuilder = SourceReference.JavaStackTraceElement
                    .newBuilder()
                    .setClassName(stackReference.className())
                    .setMethodName(stackReference.methodName());
            stackReference.fileName().ifPresent(stackTraceElementBuilder::setFileName);
            sourceReferenceBuilder
                    .setJavaStackTraceElement(stackTraceElementBuilder)
                    .setLocation(Location.newBuilder()
                            .setLine(stackReference.lineNumber()));
        }
        return sourceReferenceBuilder;
    }

    private StepDefinitionPatternType getExpressionType(CoreStepDefinition stepDefinition) {
        Class<? extends Expression> expressionType = stepDefinition.getExpression().getExpressionType();
        if (expressionType.isAssignableFrom(RegularExpression.class)) {
            return StepDefinitionPatternType.REGULAR_EXPRESSION;
        } else if (expressionType.isAssignableFrom(CucumberExpression.class)) {
            return StepDefinitionPatternType.CUCUMBER_EXPRESSION;
        } else {
            throw new IllegalArgumentException(expressionType.getName());
        }
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

        // Step definition arguments consists of parameters included in the step
        // text and
        // gherkin step arguments (doc string and data table) which are not
        // included in
        // the step text. As such the step definition arguments can not be
        // cached and
        // must be recreated each time.
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        return new PickleStepDefinitionMatch(arguments, coreStepDefinition, uri, step);
    }

    private PickleStepDefinitionMatch findStepDefinitionMatch(URI uri, Step step)
            throws AmbiguousStepDefinitionsException {
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
                result.add(new PickleStepDefinitionMatch(arguments, coreStepDefinition, uri, step));
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
                ScenarioScoped scenarioScoped = (ScenarioScoped) glue;
                scenarioScoped.dispose();
                glueIterator.remove();
            }
        }
    }

}
