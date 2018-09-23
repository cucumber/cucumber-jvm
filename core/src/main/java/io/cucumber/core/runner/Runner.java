package io.cucumber.core.runner;

import io.cucumber.core.api.event.HookType;
import io.cucumber.core.api.plugin.StepDefinitionReporter;
import io.cucumber.core.api.event.SnippetsSuggestedEvent;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.event.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Runner {
    private final CachingGlue glue = new CachingGlue();
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final Options options;

    public Runner(EventBus bus, Collection<? extends Backend> backends, Options options) {
        this.bus = bus;
        this.options = options;
        this.backends = backends;
        for (Backend backend : backends) {
            backend.loadGlue(glue, options.getGlue());
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
        glue.removeScenarioScopedGlue();
    }

    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        glue.reportStepDefinitions(stepDefinitionReporter);
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
        return new TestCase(testSteps, beforeHooks, afterHooks, pickleEvent, options.isDryRun());
    }

    private void addTestStepsForPickleSteps(List<PickleStepTestStep> testSteps, PickleEvent pickleEvent) {
        for (PickleStep step : pickleEvent.pickle.getSteps()) {
            PickleStepDefinitionMatch match;
            try {
                match = glue.stepDefinitionMatch(pickleEvent.uri, step);
                if (match == null) {
                    List<String> snippets = new ArrayList<>();
                    for (Backend backend : backends) {
                        List<String> snippet = backend.getSnippet(step, "**KEYWORD**", options.getSnippetType().getFunctionNameGenerator());
                        snippets.addAll(snippet);
                    }
                    if (!snippets.isEmpty()) {
                        bus.send(new SnippetsSuggestedEvent(bus.getTime(), pickleEvent.uri, step.getLocations(), snippets));
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
        for (Backend backend : backends) {
            backend.buildWorld();
        }
    }

    private void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }
}
