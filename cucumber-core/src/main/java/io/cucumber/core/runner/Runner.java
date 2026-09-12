package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.exception.UnrecoverableExceptions;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Snippet;
import io.cucumber.messages.types.TestRunHookFinished;
import io.cucumber.messages.types.TestRunHookStarted;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static io.cucumber.core.exception.ExceptionUtils.throwAsUncheckedException;
import static io.cucumber.core.runner.StackManipulation.removeFrameworkFrames;
import static io.cucumber.messages.Convertor.toMessage;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public final class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private final CachingGlue glue;
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final Options runnerOptions;
    private final ObjectFactory objectFactory;
    private final List<SnippetGenerator> snippetGenerators = new ArrayList<>(1);
    private @Nullable UUID testRunStartedId;

    public Runner(
            EventBus bus, Collection<? extends Backend> backends, ObjectFactory objectFactory, Options runnerOptions
    ) {
        this.bus = bus;
        this.runnerOptions = runnerOptions;
        this.backends = backends;
        this.glue = new CachingGlue(bus);
        this.objectFactory = objectFactory;
        var request = runnerOptions.getGlueDiscoveryRequest();
        for (Backend backend : backends) {
            log.debug(() -> "Loading glue for backend " + backend.getClass().getName());

            backend.loadGlue(this.glue, request);
        }
    }

    public EventBus getBus() {
        return bus;
    }

    public void setTestRunStartedId(@Nullable UUID testRunStartedId) {
        this.testRunStartedId = testRunStartedId;
    }

    public void runPickle(Pickle pickle) {
        try {

            // Java8 step definitions will be added to the glue here
            buildBackendWorlds();

            glue.prepareGlue(localeForPickle(pickle));
            snippetGenerators.clear();
            snippetGenerators
                    .addAll(createSnippetGeneratorsForPickle(pickle.getLanguage(), glue.getStepTypeRegistry()));

            TestCase testCase = createTestCaseForPickle(pickle);
            testCase.run(bus);
        } finally {
            glue.removeScenarioScopedGlue();
            disposeBackendWorlds();
        }
    }

    private Locale localeForPickle(Pickle pickle) {
        String language = pickle.getLanguage();
        return new Locale.Builder().setLanguage(language).build();
    }

    public void runBeforeAllHooks(String testRunStartedId) {
        executeTestRunHooks(testRunStartedId, glue.getBeforeAllHooks());
    }

    public void runAfterAllHooks(String testRunStartedId) {
        executeTestRunHooks(testRunStartedId, glue.getAfterAllHooks());
    }

    private void executeTestRunHooks(String testRunStartedId, List<CoreStaticHookDefinition> afterAllHooks) {
        ThrowableCollector throwableCollector = new ThrowableCollector();
        for (CoreStaticHookDefinition staticHookDefinition : afterAllHooks) {
            throwableCollector.execute(() -> executeTestRunHook(testRunStartedId, staticHookDefinition));
        }
        Throwable throwable = throwableCollector.getThrowable();
        if (throwable != null) {
            throwAsUncheckedException(throwable);
        }
    }

    private void executeTestRunHook(String testRunStartedId, CoreStaticHookDefinition hookDefinition) {
        if (runnerOptions.isDryRun()) {
            return;
        }
        var start = bus.getInstant();
        var testRunHookStartedId = bus.generateId().toString();
        bus.send(Envelope.of(new TestRunHookStarted(
            testRunHookStartedId,
            testRunStartedId,
            hookDefinition.getId().toString(),
            Thread.currentThread().getName(),
            toMessage(start))));

        try {
            hookDefinition.execute();
        } catch (CucumberBackendException e) {
            var throwable = new CucumberException("""
                    Could not invoke hook defined at '%s'.
                    It appears there was a problem with the hook definition."""
                    .formatted(hookDefinition.getLocation()),
                e);
            emitTestRunHookFinished(start, throwable, testRunHookStartedId);
            throw throwable;
        } catch (CucumberInvocationTargetException e) {
            emitTestRunHookFinished(start, removeFrameworkFrames(e), testRunHookStartedId);
            throw e;
        } catch (Throwable e) {
            UnrecoverableExceptions.rethrowIfUnrecoverable(e);
            emitTestRunHookFinished(start, e, testRunHookStartedId);
            throw e;
        }
    }

    private void emitTestRunHookFinished(Instant start, @Nullable Throwable throwable, String testRunHookStartedId) {
        var finish = bus.getInstant();
        var result = new TestStepResult(
            toMessage(Duration.between(start, finish)),
            throwable == null ? null : throwable.getMessage(),
            // TODO: Skip hooks?
            throwable == null ? TestStepResultStatus.PASSED : TestStepResultStatus.FAILED,
            throwable == null ? null : toMessage(throwable));
        bus.send(Envelope.of(new TestRunHookFinished(testRunHookStartedId, result, toMessage(finish))));
    }

    private List<SnippetGenerator> createSnippetGeneratorsForPickle(
            String language, StepTypeRegistry stepTypeRegistry
    ) {
        return backends.stream()
                .map(Backend::getSnippet)
                .filter(Objects::nonNull)
                .map(s -> new SnippetGenerator(language, s, stepTypeRegistry.parameterTypeRegistry()))
                .collect(toList());
    }

    private void buildBackendWorlds() {
        objectFactory.start();
        for (Backend backend : backends) {
            backend.buildWorld();
        }
    }

    private TestCase createTestCaseForPickle(Pickle pickle) {
        if (pickle.getSteps().isEmpty()) {
            return new TestCase(bus.generateId(), testRunStartedId, emptyList(), emptyList(), emptyList(), pickle,
                runnerOptions.isDryRun());
        }

        List<PickleStepTestStep> testSteps = createTestStepsForPickleSteps(pickle);
        List<HookTestStep> beforeHooks = createTestStepsForBeforeHooks(pickle.getTags());
        List<HookTestStep> afterHooks = createTestStepsForAfterHooks(pickle.getTags());
        return new TestCase(bus.generateId(), testRunStartedId, testSteps, beforeHooks, afterHooks, pickle,
            runnerOptions.isDryRun());
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
        List<Snippet> snippets = generateSnippetsForStep(step);
        if (snippets.isEmpty()) {
            return;
        }

        bus.send(new SnippetsSuggestedEvent(
            bus.getInstant(),
            pickle.getUri(),
            pickle.getLocation(),
            step.getLocation(),
            new Suggestion(
                step.getText(),
                snippets.stream()
                        .map(Snippet::getCode)
                        .collect(toList()))));

        bus.send(
            Envelope.of(
                new io.cucumber.messages.types.Suggestion(
                    bus.generateId().toString(),
                    step.getId(),
                    snippets)));
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
                .collect(toList());
    }

    private List<Snippet> generateSnippetsForStep(Step step) {
        return snippetGenerators.stream()
                .flatMap(generator -> generator.getSnippet(step, runnerOptions.getSnippetType())
                        .stream()
                        .map(code -> new Snippet(generator.getLanguage().orElse("unknown"), code)))
                .collect(toList());
    }

}
