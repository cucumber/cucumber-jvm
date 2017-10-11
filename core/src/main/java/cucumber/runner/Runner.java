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
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.util.log.Logger;
import cucumber.util.log.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    private final Glue glue;
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final RuntimeOptions runtimeOptions;

    public Runner(Glue glue, EventBus bus, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        LOGGER.info("Starting Runner...");
        this.glue = glue;
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
        this.backends = backends;
        LOGGER.info("Number of Backends: {}", backends.size());
        for (Backend backend : backends) {
            LOGGER.info("Backend: {}", backend.getClass());
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

        StepDefinitionMatch match = glue.stepDefinitionMatch(featurePath, step);
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
        addTestStepsForHooks(testSteps, tags, glue.getBeforeHooks(), HookType.Before);
    }

    private void addTestStepsForAfterHooks(List<TestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, glue.getAfterHooks(), HookType.After);
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
            backend.buildWorld();
        }
    }

    private void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }
}
