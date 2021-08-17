package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.cucumber.core.exception.ExceptionUtils.throwAsUncheckedException;
import static io.cucumber.core.runner.StackManipulation.removeFrameworkFrames;
import static java.util.Collections.emptyList;

public final class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private final CachingGlue glue;
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final Options runnerOptions;
    private final ObjectFactory objectFactory;
    private List<SnippetGenerator> snippetGenerators;

    public Runner(
            EventBus bus, Collection<? extends Backend> backends, ObjectFactory objectFactory, Options runnerOptions
    ) {
        this.bus = bus;
        this.runnerOptions = runnerOptions;
        this.backends = backends;
        this.glue = new CachingGlue(bus);
        this.objectFactory = objectFactory;
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

            // Java8 step definitions will be added to the glue here
            buildBackendWorlds();

            glue.prepareGlue(stepTypeRegistry);

            TestCase testCase = createTestCaseForPickle(pickle);
            testCase.run(bus);
        } finally {
            glue.removeScenarioScopedGlue();
            disposeBackendWorlds();
        }
    }

    private StepTypeRegistry createTypeRegistryForPickle(Pickle pickle) {
        String language = pickle.getLanguage();
        Locale locale = new Locale(language);
        return new StepTypeRegistry(locale);
    }

    public void runBeforeAllHooks() {
        executeHooks(glue.getBeforeAllHooks());
    }

    public void runAfterAllHooks() {
        executeHooks(glue.getAfterAllHooks());
    }

    private void executeHooks(List<StaticHookDefinition> afterAllHooks) {
        ThrowableCollector throwableCollector = new ThrowableCollector();
        for (StaticHookDefinition staticHookDefinition : afterAllHooks) {
            throwableCollector.execute(() -> executeHook(staticHookDefinition));
        }
        Throwable throwable = throwableCollector.getThrowable();
        if (throwable != null) {
            throwAsUncheckedException(throwable);
        }
    }

    private void executeHook(StaticHookDefinition hookDefinition) {
        if (runnerOptions.isDryRun()) {
            return;
        }
        try {
            hookDefinition.execute();
        } catch (CucumberBackendException e) {
            CucumberException exception = new CucumberException(String.format("" +
                    "Could not invoke hook defined at '%s'.\n" +
                    "It appears there was a problem with the hook definition.",
                hookDefinition.getLocation()), e);
            throwAsUncheckedException(exception);
        } catch (CucumberInvocationTargetException e) {
            Throwable throwable = removeFrameworkFrames(e);
            throwAsUncheckedException(throwable);
        }
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
            emitSnippetSuggestedEvent(pickle, step);
            return new UndefinedPickleStepDefinitionMatch(pickle.getUri(), step);
        } catch (AmbiguousStepDefinitionsException e) {
            return new AmbiguousPickleStepDefinitionsMatch(pickle.getUri(), step, e);
        }
    }

    private void emitSnippetSuggestedEvent(Pickle pickle, Step step) {
        List<String> snippets = generateSnippetsForStep(step);
        if (snippets.isEmpty()) {
            return;
        }
        Suggestion suggestion = new Suggestion(step.getText(), snippets);
        Location scenarioLocation = pickle.getLocation();
        Location stepLocation = step.getLocation();
        SnippetsSuggestedEvent event = new SnippetsSuggestedEvent(bus.getInstant(), pickle.getUri(), scenarioLocation,
            stepLocation, suggestion);
        bus.send(event);
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
