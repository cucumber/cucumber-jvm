package io.cucumber.core.runner;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public final class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private final CachingGlue glue;
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final Options runnerOptions;
    private final ObjectFactory objectFactory;
    private final TypeRegistryConfigurer typeRegistryConfigurer;
    private List<SnippetGenerator> snippetGenerators;

    public Runner(
            EventBus bus, Collection<? extends Backend> backends, ObjectFactory objectFactory,
            TypeRegistryConfigurer typeRegistryConfigurer, Options runnerOptions
    ) {
        this.bus = bus;
        this.runnerOptions = runnerOptions;
        this.backends = backends;
        this.glue = new CachingGlue(bus);
        this.objectFactory = objectFactory;
        this.typeRegistryConfigurer = typeRegistryConfigurer;
        List<URI> gluePaths = runnerOptions.getGlue();
        log.debug(() -> "Loading glue from " + gluePaths);
        for (Backend backend : backends) {
            log.debug(() -> "Loading glue for backend " + backend.getClass().getName());
            backend.loadGlue(this.glue, gluePaths);
        }
    }

    public EventBus getBus() {
        return bus;
    }

    public void runPickle(Pickle pickle) {
        try {
            StepTypeRegistry stepTypeRegistry = createTypeRegistryForPickle(pickle);
            snippetGenerators = createSnippetGeneratorsForPickle(stepTypeRegistry);

            buildBackendWorlds(); // Java8 step definitions will be added to the
                                  // glue here

            glue.prepareGlue(stepTypeRegistry);

            TestCase testCase = createTestCaseForPickle(pickle);
            testCase.run(bus);
        } finally {
            glue.removeScenarioScopedGlue();
            disposeBackendWorlds();
        }
    }

    private StepTypeRegistry createTypeRegistryForPickle(Pickle pickle) {
        Locale locale = typeRegistryConfigurer.locale();
        if (locale == null) {
            locale = new Locale(pickle.getLanguage());
        }
        StepTypeRegistry stepTypeRegistry = new StepTypeRegistry(locale);
        typeRegistryConfigurer.configureTypeRegistry(stepTypeRegistry);
        return stepTypeRegistry;
    }

    private List<SnippetGenerator> createSnippetGeneratorsForPickle(StepTypeRegistry stepTypeRegistry) {
        return backends.stream()
                .map(Backend::getSnippet)
                .filter(Objects::nonNull)
                .map(s -> new SnippetGenerator(s, stepTypeRegistry.parameterTypeRegistry()))
                .collect(Collectors.toList());
    }

    private void buildBackendWorlds() {
        objectFactory.start();
        for (Backend backend : backends) {
            backend.buildWorld();
        }
    }

    private TestCase createTestCaseForPickle(Pickle pickle) {
        if (pickle.getSteps().isEmpty()) {
            return new TestCase(bus.generateId(), emptyList(), emptyList(), emptyList(), pickle,
                runnerOptions.isDryRun());
        }

        List<PickleStepTestStep> testSteps = createTestStepsForPickleSteps(pickle);
        List<HookTestStep> beforeHooks = createTestStepsForBeforeHooks(pickle.getTags());
        List<HookTestStep> afterHooks = createTestStepsForAfterHooks(pickle.getTags());
        return new TestCase(bus.generateId(), testSteps, beforeHooks, afterHooks, pickle, runnerOptions.isDryRun());
    }

    private void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
        objectFactory.stop();
    }

    private List<PickleStepTestStep> createTestStepsForPickleSteps(Pickle pickle) {
        List<PickleStepTestStep> testSteps = new ArrayList<>();

        for (Step step : pickle.getSteps()) {
            PickleStepDefinitionMatch match = matchStepToStepDefinition(pickle, step);
            List<HookTestStep> afterStepHookSteps = createAfterStepHooks(pickle.getTags());
            List<HookTestStep> beforeStepHookSteps = createBeforeStepHooks(pickle.getTags());
            testSteps.add(new PickleStepTestStep(bus.generateId(), pickle.getUri(), step, beforeStepHookSteps,
                afterStepHookSteps, match));
        }

        return testSteps;
    }

    private List<HookTestStep> createTestStepsForBeforeHooks(List<String> tags) {
        return createTestStepsForHooks(tags, glue.getBeforeHooks(), HookType.BEFORE);
    }

    private List<HookTestStep> createTestStepsForAfterHooks(List<String> tags) {
        return createTestStepsForHooks(tags, glue.getAfterHooks(), HookType.AFTER);
    }

    private PickleStepDefinitionMatch matchStepToStepDefinition(Pickle pickle, Step step) {
        try {
            PickleStepDefinitionMatch match = glue.stepDefinitionMatch(pickle.getUri(), step);
            if (match != null) {
                return match;
            }
            List<String> snippets = generateSnippetsForStep(step);
            if (!snippets.isEmpty()) {
                bus.send(new SnippetsSuggestedEvent(bus.getInstant(), pickle.getUri(), pickle.getScenarioLocation(),
                    step.getLocation(), snippets));
            }
            return new UndefinedPickleStepDefinitionMatch(pickle.getUri(), step);
        } catch (AmbiguousStepDefinitionsException e) {
            return new AmbiguousPickleStepDefinitionsMatch(pickle.getUri(), step, e);
        }
    }

    private List<HookTestStep> createAfterStepHooks(List<String> tags) {
        return createTestStepsForHooks(tags, glue.getAfterStepHooks(), HookType.AFTER_STEP);
    }

    private List<HookTestStep> createBeforeStepHooks(List<String> tags) {
        return createTestStepsForHooks(tags, glue.getBeforeStepHooks(), HookType.BEFORE_STEP);
    }

    private List<HookTestStep> createTestStepsForHooks(
            List<String> tags, Collection<CoreHookDefinition> hooks, HookType hookType
    ) {
        return hooks.stream()
                .filter(hook -> hook.matches(tags))
                .map(hook -> new HookTestStep(bus.generateId(), hookType, new HookDefinitionMatch(hook)))
                .collect(Collectors.toList());
    }

    private List<String> generateSnippetsForStep(Step step) {
        List<String> snippets = new ArrayList<>();
        for (SnippetGenerator snippetGenerator : snippetGenerators) {
            List<String> snippet = snippetGenerator.getSnippet(step, runnerOptions.getSnippetType());
            snippets.addAll(snippet);
        }
        return snippets;
    }

}
