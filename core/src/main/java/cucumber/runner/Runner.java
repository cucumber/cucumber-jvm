package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.SnippetsSuggestedEvent;
import cucumber.runtime.AmbiguousPickleStepDefinitionsMatch;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.Backend;
import cucumber.runtime.FailedPickleStepInstantiationMatch;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.HookDefinitionMatch;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.PickleStepDefinitionMatch;
import cucumber.runtime.UndefinedPickleStepDefinitionMatch;
import cucumber.runtime.UnreportedStepExecutor;
import gherkin.events.PickleEvent;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Runner implements UnreportedStepExecutor {
    private final Glue glue;
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final RuntimeOptions runtimeOptions;

    public Runner(Glue glue, EventBus bus, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        this.glue = glue;
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
        this.backends = backends;
        for (Backend backend : backends) {
            backend.loadGlue(glue, runtimeOptions.getGlue());
            backend.setUnreportedStepExecutor(this);
        }

    }

    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    @Override
    public void runUnreportedStep(String featurePath, String language, String stepName, int line, List<PickleRow> dataTableRows, PickleString docString) throws Throwable {
        List<Argument> arguments = new ArrayList<Argument>();
        if (dataTableRows != null && !dataTableRows.isEmpty()) {
            arguments.add(new PickleTable(dataTableRows));
        } else if (docString != null) {
            arguments.add(docString);
        }
        PickleStep step = new PickleStep(stepName, arguments, Collections.<PickleLocation>emptyList());

        PickleStepDefinitionMatch match = glue.stepDefinitionMatch(featurePath, step);
        if (match == null) {
            UndefinedStepException error = new UndefinedStepException(step);

            StackTraceElement[] originalTrace = error.getStackTrace();
            StackTraceElement[] newTrace = new StackTraceElement[originalTrace.length + 1];
            newTrace[0] = new StackTraceElement("✽", "StepDefinition", featurePath, line);
            System.arraycopy(originalTrace, 0, newTrace, 1, originalTrace.length);
            error.setStackTrace(newTrace);

            throw error;
        }
        match.runStep(language, null);
    }

    public void runPickle(PickleEvent pickle) {
        buildBackendWorlds(); // Java8 step definitions will be added to the glue here
        TestCase testCase = createTestCaseForPickle(pickle);
        testCase.run(bus);
        disposeBackendWorlds();
    }

    public Glue getGlue() {
        return glue;
    }


    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        glue.reportStepDefinitions(stepDefinitionReporter);
    }

    private TestCase createTestCaseForPickle(PickleEvent pickleEvent) {
        List<PickleStepTestStep> testSteps = new ArrayList<PickleStepTestStep>();
        List<HookTestStep> beforeHooks = new ArrayList<HookTestStep>();
        List<HookTestStep> afterHooks = new ArrayList<HookTestStep>();
        if (!pickleEvent.pickle.getSteps().isEmpty()) {
            addTestStepsForBeforeHooks(beforeHooks, pickleEvent.pickle.getTags());
            addTestStepsForPickleSteps(testSteps, pickleEvent);
            addTestStepsForAfterHooks(afterHooks, pickleEvent.pickle.getTags());
        }
        return new TestCase(testSteps, beforeHooks, afterHooks, pickleEvent, runtimeOptions.isDryRun());
    }

    private void addTestStepsForPickleSteps(List<PickleStepTestStep> testSteps, PickleEvent pickleEvent) {
        for (PickleStep step : pickleEvent.pickle.getSteps()) {
            PickleStepDefinitionMatch match;
            try {
                match = glue.stepDefinitionMatch(pickleEvent.uri, step);
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
        List<HookTestStep> hookSteps = new ArrayList<HookTestStep>();
        addTestStepsForHooks(hookSteps, tags, glue.getAfterStepHooks(), HookType.AfterStep);
        return hookSteps;
    }

    private List<HookTestStep> getBeforeStepHooks(List<PickleTag> tags) {
        List<HookTestStep> hookSteps = new ArrayList<HookTestStep>();
        addTestStepsForHooks(hookSteps, tags, glue.getBeforeStepHooks(), HookType.BeforeStep);
        return hookSteps;
    }

    private void buildBackendWorlds() {
        runtimeOptions.getPlugins(); // To make sure that the plugins are instantiated after
        // the features have been parsed but before the pickles starts to execute.
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
