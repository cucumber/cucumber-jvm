package io.cucumber.core.runner;

import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.event.HookType;
import io.cucumber.core.event.SnippetsSuggestedEvent;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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

    public Runner(EventBus bus, Collection<? extends Backend> backends, ObjectFactory objectFactory, TypeRegistryConfigurer typeRegistryConfigurer, Options runnerOptions) {
        this.bus = bus;
        this.runnerOptions = runnerOptions;
        this.backends = backends;
        this.glue = new CachingGlue(bus);
        this.objectFactory = objectFactory;
        this.typeRegistryConfigurer = typeRegistryConfigurer;
        List<URI> gluePaths = runnerOptions.getGlue();
        log.debug("Loading glue from " + gluePaths);
        for (Backend backend : backends) {
            log.debug("Loading glue for backend " + backend.getClass().getName());
            backend.loadGlue(this.glue, gluePaths);
        }
    }

    public EventBus getBus() {
        return bus;
    }

    public void runPickle(PickleEvent pickle) {
        try {
            TypeRegistry typeRegistry = createTypeRegistryForPickle(pickle);
            snippetGenerators = createSnippetGeneratorsForPickle(typeRegistry);

            buildBackendWorlds(); // Java8 step definitions will be added to the glue here

            glue.prepareGlue(typeRegistry);

            TestCase testCase = createTestCaseForPickle(pickle);
            testCase.run(bus);
        } finally {
            glue.removeScenarioScopedGlue();
            disposeBackendWorlds();
        }
    }

    private List<SnippetGenerator> createSnippetGeneratorsForPickle(TypeRegistry typeRegistry) {
        return backends.stream()
            .map(Backend::getSnippet)
            .map(s -> new SnippetGenerator(s, typeRegistry.parameterTypeRegistry()))
            .collect(Collectors.toList());
    }

    private TypeRegistry createTypeRegistryForPickle(PickleEvent pickle) {
        Locale locale = typeRegistryConfigurer.locale();
        if(locale == null){
            locale = new Locale(pickle.pickle.getLanguage());
        }
        TypeRegistry typeRegistry = new TypeRegistry(locale);
        typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
        return typeRegistry;
    }

    private TestCase createTestCaseForPickle(PickleEvent pickleEvent) {
        if (pickleEvent.pickle.getSteps().isEmpty()) {
            return new TestCase(emptyList(), emptyList(), emptyList(), pickleEvent, runnerOptions.isDryRun());
        }

        List<PickleStepTestStep> testSteps = createTestStepsForPickleSteps(pickleEvent);
        List<HookTestStep> beforeHooks = createTestStepsForBeforeHooks(pickleEvent.pickle.getTags());
        List<HookTestStep> afterHooks = createTestStepsForAfterHooks(pickleEvent.pickle.getTags());
        return new TestCase(testSteps, beforeHooks, afterHooks, pickleEvent, runnerOptions.isDryRun());
    }

    private List<PickleStepTestStep> createTestStepsForPickleSteps(PickleEvent pickleEvent) {
        List<PickleStepTestStep> testSteps = new ArrayList<>();

        for (PickleStep step : pickleEvent.pickle.getSteps()) {
            PickleStepDefinitionMatch match;
            try {
                match = glue.stepDefinitionMatch(pickleEvent.uri, step);
                if (match == null) {
                    List<String> snippets = new ArrayList<>();
                    for (SnippetGenerator snippetGenerator : snippetGenerators) {
                        List<String> snippet = snippetGenerator.getSnippet(step, "**KEYWORD**", runnerOptions.getSnippetType());
                        snippets.addAll(snippet);
                    }
                    if (!snippets.isEmpty()) {
                        bus.send(new SnippetsSuggestedEvent(bus.getInstant(), pickleEvent.uri, locations(step), snippets));
                    }
                    match = new UndefinedPickleStepDefinitionMatch(step);
                }
            } catch (AmbiguousStepDefinitionsException e) {
                match = new AmbiguousPickleStepDefinitionsMatch(pickleEvent.uri, step, e);
            } catch (Throwable t) {
                match = new FailedPickleStepInstantiationMatch(pickleEvent.uri, step, t);
            }


            List<HookTestStep> afterStepHookSteps = createAfterStepHooks(pickleEvent.pickle.getTags());
            List<HookTestStep> beforeStepHookSteps = createBeforeStepHooks(pickleEvent.pickle.getTags());
            testSteps.add(new PickleStepTestStep(pickleEvent.uri, step, beforeStepHookSteps, afterStepHookSteps, match));
        }

        return testSteps;
    }

    private List<SnippetsSuggestedEvent.Location> locations(PickleStep step) {
        return step.getLocations().stream()
            .map(p -> new SnippetsSuggestedEvent.Location(p.getLine(), p.getLine()))
            .collect(Collectors.toList());
    }

    private List<HookTestStep> createTestStepsForBeforeHooks(List<PickleTag> tags) {
        return createTestStepsForHooks(tags, glue.getBeforeHooks(), HookType.BEFORE);
    }

    private List<HookTestStep> createTestStepsForAfterHooks(List<PickleTag> tags) {
        return createTestStepsForHooks(tags, glue.getAfterHooks(), HookType.AFTER);
    }

    private List<HookTestStep> createTestStepsForHooks(List<PickleTag> pickleTags, Collection<CoreHookDefinition> hooks, HookType hookType) {
        List<String> tags = pickleTags.stream().map(PickleTag::getName).collect(Collectors.toList());
        return hooks.stream()
            .filter(hook -> hook.matches(tags))
            .map(hook -> new HookTestStep(hookType, new HookDefinitionMatch(hook)))
            .collect(Collectors.toList());
    }

    private List<HookTestStep> createAfterStepHooks(List<PickleTag> tags) {
        return createTestStepsForHooks(tags, glue.getAfterStepHooks(), HookType.AFTER_STEP);
    }

    private List<HookTestStep> createBeforeStepHooks(List<PickleTag> tags) {
        return createTestStepsForHooks(tags, glue.getBeforeStepHooks(), HookType.BEFORE_STEP);
    }

    private void buildBackendWorlds() {
        objectFactory.start();
        for (Backend backend : backends) {
            backend.buildWorld();
        }
    }

    private void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
        objectFactory.stop();
    }
}
