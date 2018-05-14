package cucumber.runner;

import cucumber.api.Argument;
import cucumber.api.HookType;
import cucumber.api.Scenario;
import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RunnerTest {
    private static final String ENGLISH = "en";
    private static final String NAME = "name";
    private static final List<PickleStep> NO_STEPS = Collections.<PickleStep>emptyList();
    private static final List<PickleTag> NO_TAGS = Collections.<PickleTag>emptyList();
    private static final List<PickleLocation> MOCK_LOCATIONS = asList(mock(PickleLocation.class));
    private final Backend backend = mock(Backend.class);
    private final Runtime runtime = createRuntime(backend);
    private final Runner runner = runtime.getRunner();

    @Test
    public void hooks_execute_when_world_exist() throws Throwable {
        PickleStep step = mock(PickleStep.class);
        HookDefinition beforeHook = addBeforeHook(runtime);
        HookDefinition afterHook = addAfterHook(runtime);

        runner.runPickle(createPickleEventWithSteps(asList(step)));

        InOrder inOrder = inOrder(beforeHook, afterHook, backend);
        inOrder.verify(backend).buildWorld(Matchers.<Glue>any());
        inOrder.verify(beforeHook).execute(Matchers.<Scenario>any());
        inOrder.verify(afterHook).execute(Matchers.<Scenario>any());
        inOrder.verify(backend).disposeWorld(Matchers.<Glue>any());
    }

    @Test
    public void steps_are_skipped_after_failure() throws Throwable {
        HookDefinition failingBeforeHook = addBeforeHook(runtime);
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(Matchers.<Scenario>any());
        StepDefinition stepDefinition = mock(StepDefinition.class);

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition), runtime));

        InOrder inOrder = inOrder(failingBeforeHook, stepDefinition);
        inOrder.verify(failingBeforeHook).execute(Matchers.<Scenario>any());
        inOrder.verify(stepDefinition, never()).execute(Matchers.anyString(), Matchers.<Object[]>any());
    }

    @Test
    public void aftersteps_are_executed_after_failed_step() throws Throwable {

        StepDefinition stepDefinition = mock(StepDefinition.class);
        doThrow(RuntimeException.class).when(stepDefinition).execute(Matchers.anyString(), Matchers.<Object[]>any());
        HookDefinition afteStepHook = addAfterStepHook(runtime);

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition), runtime));

        InOrder inOrder = inOrder(afteStepHook, stepDefinition);
        inOrder.verify(stepDefinition).execute(Matchers.anyString(), Matchers.<Object[]>any());
        inOrder.verify(afteStepHook).execute(Matchers.<Scenario>any());
    }

    @Test
    public void aftersteps_executed_for_passed_step() throws Throwable {

        StepDefinition stepDefinition = mock(StepDefinition.class);
        HookDefinition afteStepHook1 = addAfterStepHook(runtime);
        HookDefinition afteStepHook2 = addAfterStepHook(runtime);

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition), runtime));

        InOrder inOrder = inOrder(afteStepHook1, afteStepHook2, stepDefinition);
        inOrder.verify(stepDefinition).execute(Matchers.anyString(), Matchers.<Object[]>any());
        inOrder.verify(afteStepHook1).execute(Matchers.<Scenario>any());
        inOrder.verify(afteStepHook2).execute(Matchers.<Scenario>any());
    }

    @Test
    public void hooks_execute_also_after_failure() throws Throwable {
        PickleStep step = mock(PickleStep.class);
        HookDefinition failingBeforeHook = addBeforeHook(runtime);
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(Matchers.<Scenario>any());
        HookDefinition beforeHook = addBeforeHook(runtime);
        HookDefinition afterHook = addAfterHook(runtime);

        runner.runPickle(createPickleEventWithSteps(asList(step)));

        InOrder inOrder = inOrder(failingBeforeHook, beforeHook, afterHook);
        inOrder.verify(failingBeforeHook).execute(Matchers.<Scenario>any());
        inOrder.verify(beforeHook).execute(Matchers.<Scenario>any());
        inOrder.verify(afterHook).execute(Matchers.<Scenario>any());
    }

    @Test
    public void steps_are_executed() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        runtime.getRunner().runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition), runtime));
        verify(stepDefinition).execute(Matchers.anyString(), Matchers.<Object[]>any());
    }

    @Test
    public void steps_are_not_executed_on_dry_run() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        final Runtime dryRuntime = createRuntime(backend, "--dry-run");
        dryRuntime.getRunner().runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition), dryRuntime));
        verify(stepDefinition, never()).execute(Matchers.anyString(), Matchers.<Object[]>any());
    }

    @Test
    public void hooks_not_executed_in_dry_run_mode() throws Throwable {
        Runtime runtime = createRuntime(backend, "--dry-run");
        Runner runner = runtime.getRunner();
        PickleStep step = mock(PickleStep.class);
        HookDefinition beforeHook = addBeforeHook(runtime);
        HookDefinition afterHook = addAfterHook(runtime);
        HookDefinition afterStepHook = addAfterStepHook(runtime);

        runner.runPickle(createPickleEventWithSteps(asList(step)));

        verify(beforeHook, never()).execute(Matchers.<Scenario>any());
        verify(afterStepHook, never()).execute(Matchers.<Scenario>any());
        verify(afterHook, never()).execute(Matchers.<Scenario>any());
    }

    @Test
    public void hooks_not_executed_for_empty_pickles() throws Throwable {
        HookDefinition beforeHook = addBeforeHook(runtime);
        HookDefinition afterHook = addAfterHook(runtime);
        HookDefinition afterStepHook = addAfterStepHook(runtime);

        runner.runPickle(createEmptyPickleEvent());

        verify(beforeHook, never()).execute(Matchers.<Scenario>any());
        verify(afterStepHook, never()).execute(Matchers.<Scenario>any());
        verify(afterHook, never()).execute(Matchers.<Scenario>any());
    }

    @Test
    public void backends_are_asked_for_snippets_for_undefined_steps() throws Throwable {
        PickleStep step = mock(PickleStep.class);
        runner.runPickle(createPickleEventWithSteps(asList(step)));

        verify(backend).getSnippet(Matchers.eq(step), Matchers.anyString(), Matchers.<FunctionNameGenerator>any());
    }

    private Runtime createRuntime(Backend backend) {
        return createRuntime(backend, "-p null");
    }

    private Runtime createRuntime(Backend backend, String options) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(options);
        return new Runtime(new ClasspathResourceLoader(classLoader), classLoader, asList(backend), runtimeOptions);
    }

    private HookDefinition addBeforeHook(Runtime runtime) {
        return addHook(runtime, HookType.Before);
    }

    private HookDefinition addAfterHook(Runtime runtime) {
        return addHook(runtime, HookType.After);
    }

    private HookDefinition addAfterStepHook(Runtime runtime) {
        return addHook(runtime, HookType.AfterStep);
    }

    private HookDefinition addHook(Runtime runtime, HookType hookType) {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(PickleTag.class))).thenReturn(true);
        if (hookType == HookType.Before) {
            runtime.getGlue().addBeforeHook(hook);
        } else if (hookType == HookType.After){
            runtime.getGlue().addAfterHook(hook);
        } else if (hookType == HookType.AfterStep) {
            runtime.getGlue().addAfterStepHook(hook);
        }
        return hook;
    }

    private PickleEvent createEmptyPickleEvent() {
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, NO_STEPS, NO_TAGS, MOCK_LOCATIONS));
    }

    private PickleEvent createPickleEventMatchingStepDefinitions(List<StepDefinition> stepDefinitions, Runtime runtime) {
        List<PickleStep> steps = new ArrayList<PickleStep>(stepDefinitions.size());
        int i = 0;
        for (StepDefinition stepDefinition : stepDefinitions) {
            PickleStep step = mock(PickleStep.class);
            steps.add(step);
            when(stepDefinition.matchedArguments(step)).thenReturn(Collections.<Argument>emptyList());
            when(stepDefinition.getPattern()).thenReturn("pattern" + Integer.toString(++i));
            runtime.getGlue().addStepDefinition(stepDefinition);
        }
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, steps, NO_TAGS, MOCK_LOCATIONS));
    }

    private PickleEvent createPickleEventWithSteps(List<PickleStep> steps) {
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, steps, NO_TAGS, MOCK_LOCATIONS));
    }
}
