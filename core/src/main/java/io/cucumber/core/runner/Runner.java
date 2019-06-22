package io.cucumber.core.runner;

import io.cucumber.core.api.event.HookType;
import io.cucumber.core.api.event.SnippetsSuggestedEvent;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.util.FixJava;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private final CachingGlue glue;
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final Options runnerOptions;
    private final ObjectFactory objectFactory;

    public Runner(EventBus bus, Collection<? extends Backend> backends, ObjectFactory objectFactory, Options runnerOptions) {
        this.bus = bus;
        this.glue = new CachingGlue(bus);
        this.runnerOptions = runnerOptions;
        this.backends = backends;
        this.objectFactory = objectFactory;
        List<URI> gluePaths = runnerOptions.getGlue();
        log.debug("Loading glue from " + FixJava.join(gluePaths, ", "));
        for (Backend backend : backends) {
            log.debug("Loading glue for backend " + backend.getClass().getName());
            backend.loadGlue(this.glue, gluePaths);
        }
    }

    public EventBus getBus() {
        return bus;
    }

    public void runPickle(PickleEvent pickle) {
        buildBackendWorlds(); // Java8 step definitions will be added to the glue here
        TestCase testCase = createTestCaseForPickle(pickle);
        testCase.run(bus);
        disposeBackendWorlds();
    }

    private TestCase createTestCaseForPickle(PickleEvent pickleEvent) {
        List<PickleStepTestStep> testSteps = new ArrayList<>();
        List<HookTestStep> beforeHooks = new ArrayList<>();
        List<HookTestStep> afterHooks = new ArrayList<>();
        if (!pickleEvent.pickle.getSteps().isEmpty()) {
            addTestStepsForBeforeHooks(beforeHooks, pickleEvent.pickle.getTags());
            addTestStepsForPickleSteps(testSteps, pickleEvent);
            addTestStepsForAfterHooks(afterHooks, pickleEvent.pickle.getTags());
        }
        return new TestCase(testSteps, beforeHooks, afterHooks, pickleEvent, runnerOptions.isDryRun());
    }

    private void addTestStepsForPickleSteps(List<PickleStepTestStep> testSteps, PickleEvent pickleEvent) {
        for (PickleStep step : pickleEvent.pickle.getSteps()) {
            PickleStepDefinitionMatch match;
            try {
                match = glue.stepDefinitionMatch(pickleEvent.uri, step);
                if (match == null) {
                    List<String> snippets = new ArrayList<>();
                    for (Backend backend : backends) {
                        List<String> snippet = backend.getSnippet(step, "**KEYWORD**", runnerOptions.getSnippetType().getFunctionNameGenerator());
                        snippets.addAll(snippet);
                    }
                    if (!snippets.isEmpty()) {
                        bus.send(new SnippetsSuggestedEvent(bus.getInstant(), pickleEvent.uri, step.getLocations(), snippets));
                    }
                    match = new UndefinedPickleStepDefinitionMatch(step);
                }
            } catch (AmbiguousStepDefinitionsException e) {
                match = new AmbiguousPickleStepDefinitionsMatch(pickleEvent.uri, step, e);
            } catch (Throwable t) {
                match = new FailedPickleStepInstantiationMatch(pickleEvent.uri, step, t);
            }


            List<HookTestStep> afterStepHookSteps = getAfterStepHooks(pickleEvent.pickle.getTags());
            List<HookTestStep> beforeStepHookSteps = getBeforeStepHooks(pickleEvent.pickle.getTags());
            testSteps.add(new PickleStepTestStep(pickleEvent.uri, step, beforeStepHookSteps, afterStepHookSteps, match));
        }
    }

    private void addTestStepsForBeforeHooks(List<HookTestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, glue.getBeforeHooks(), HookType.Before);
    }

    private void addTestStepsForAfterHooks(List<HookTestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, glue.getAfterHooks(), HookType.After);
    }

    private void addTestStepsForHooks(List<HookTestStep> testSteps, List<PickleTag> tags, List<HookDefinition> hooks, HookType hookType) {
        for (HookDefinition hook : hooks) {
            if (hook.matches(tags)) {
                HookTestStep testStep = new HookTestStep(hookType, new HookDefinitionMatch(hook));
                testSteps.add(testStep);
            }
        }
    }

    private List<HookTestStep> getAfterStepHooks(List<PickleTag> tags) {
        List<HookTestStep> hookSteps = new ArrayList<>();
        addTestStepsForHooks(hookSteps, tags, glue.getAfterStepHooks(), HookType.AfterStep);
        return hookSteps;
    }

    private List<HookTestStep> getBeforeStepHooks(List<PickleTag> tags) {
        List<HookTestStep> hookSteps = new ArrayList<>();
        addTestStepsForHooks(hookSteps, tags, glue.getBeforeStepHooks(), HookType.BeforeStep);
        return hookSteps;
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
        glue.removeScenarioScopedGlue();
    }
}
