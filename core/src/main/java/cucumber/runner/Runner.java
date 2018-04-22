package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.SnippetsSuggestedEvent;
import cucumber.runtime.AmbiguousStepDefinitionsMatch;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.Backend;
import cucumber.runtime.FailedStepInstantiationMatch;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.HookDefinitionMatch;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.UndefinedStepDefinitionMatch;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Runner {
    private final Glue templateGlue;
    private final ThreadLocal<Glue> localGlue = new ThreadLocal<Glue>();
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final RuntimeOptions runtimeOptions;

    public Runner(Glue templateGlue, EventBus bus, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        this.templateGlue = templateGlue;
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
        this.backends = backends;
    }

    public void prepareForFeatureRun() {
        localGlue.set(templateGlue.clone());
    }

    public void runPickle(PickleEvent pickle) {
        //Guard again direct usages where ThreadLocal state not pre-set
        if (localGlue.get() == null) {
            prepareForFeatureRun();
        }
        buildBackendWorlds(); // Java8 step definitions will be added to the glue here
        TestCase testCase = createTestCaseForPickle(pickle);
        testCase.run(bus);
        disposeBackendWorlds();
    }
    
    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        //Looks like can use templateGlue here as this is called prior to executing any tests
        templateGlue.reportStepDefinitions(stepDefinitionReporter);
    }

    private TestCase createTestCaseForPickle(PickleEvent pickleEvent) {
        List<TestStep> testSteps = new ArrayList<TestStep>();
        if (!pickleEvent.pickle.getSteps().isEmpty()) {
            if (!runtimeOptions.isDryRun()) {
                addTestStepsForBeforeHooks(testSteps, pickleEvent.pickle.getTags());
            }
            addTestStepsForPickleSteps(testSteps, pickleEvent);
            if (!runtimeOptions.isDryRun()) {
                addTestStepsForAfterHooks(testSteps, pickleEvent.pickle.getTags());
            }
        }
        return new TestCase(testSteps, pickleEvent, runtimeOptions.isDryRun());
    }

    private void addTestStepsForPickleSteps(List<TestStep> testSteps, PickleEvent pickleEvent) {
        for (PickleStep step : pickleEvent.pickle.getSteps()) {
            StepDefinitionMatch match;
            try {
                match = localGlue.get().stepDefinitionMatch(pickleEvent.uri, step);
                if (match == null) {
                    List<String> snippets = new ArrayList<String>();
                    for (Backend backend : backends) {
                        String snippet = backend.getSnippet(step, "**KEYWORD**", runtimeOptions.getSnippetType().getFunctionNameGenerator());
                        if (snippet != null) {
                            snippets.add(snippet);
                        }
                    }
                    if (!snippets.isEmpty()) {
                        bus.send(new SnippetsSuggestedEvent(bus.getTime(), pickleEvent.uri, step.getLocations(), snippets));
                    }
                    match = new UndefinedStepDefinitionMatch(step);
                }
            } catch (AmbiguousStepDefinitionsException e) {
                match = new AmbiguousStepDefinitionsMatch(pickleEvent.uri, step, e);
            } catch (Throwable t) {
                match = new FailedStepInstantiationMatch(pickleEvent.uri, step, t);
            }
            testSteps.add(new PickleTestStep(pickleEvent.uri, step, match));
        }
    }

    private void addTestStepsForBeforeHooks(List<TestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, localGlue.get().getBeforeHooks(), HookType.Before);
    }

    private void addTestStepsForAfterHooks(List<TestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, localGlue.get().getAfterHooks(), HookType.After);
    }

    private void addTestStepsForHooks(List<TestStep> testSteps, List<PickleTag> tags,  List<HookDefinition> hooks, HookType hookType) {
        for (HookDefinition hook : hooks) {
            if (hook.matches(tags)) {
                TestStep testStep = new UnskipableStep(hookType, new HookDefinitionMatch(hook));
                testSteps.add(testStep);
            }
        }
    }

    private void buildBackendWorlds() {
        runtimeOptions.getPlugins(); // To make sure that the plugins are instantiated after
        // the features have been parsed but before the pickles starts to execute.
        for (Backend backend : backends) {
            backend.buildWorld(localGlue.get());
        }
    }

    private void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld(localGlue.get());
        }
    }
}
