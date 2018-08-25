package cucumber.runner;

import cucumber.runtime.Glue;
import cucumber.runtime.GlueSupplier;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.stepexpression.Argument;
import cucumber.api.HookType;
import cucumber.api.Scenario;
import cucumber.runtime.Backend;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.TestGlueHelper;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RunnerTest {
    private static final String ENGLISH = "en";
    private static final String NAME = "name";
    private static final List<PickleStep> NO_STEPS = Collections.emptyList();
    private static final List<PickleTag> NO_TAGS = Collections.emptyList();
    private static final List<PickleLocation> MOCK_LOCATIONS = asList(mock(PickleLocation.class));
    private final Backend backend = mock(Backend.class);
    private final GlueSupplier glueSupplier = new TestGlueHelper();
    private final Glue glue = glueSupplier.get();
    private final Runner runner = createRunner(backend);

    @Test
    public void hooks_execute_when_world_exist() throws Throwable {
        PickleStep step = mock(PickleStep.class);
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();

        runner.runPickle(createPickleEventWithSteps(asList(step)));

        InOrder inOrder = inOrder(beforeHook, afterHook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(beforeHook).execute(any(Scenario.class));
        inOrder.verify(afterHook).execute(any(Scenario.class));
        inOrder.verify(backend).disposeWorld();
    }

    @Test
    public void steps_are_skipped_after_failure() throws Throwable {
        HookDefinition failingBeforeHook = addBeforeHook();
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(any(Scenario.class));
        StepDefinition stepDefinition = mock(StepDefinition.class);

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));

        InOrder inOrder = inOrder(failingBeforeHook, stepDefinition);
        inOrder.verify(failingBeforeHook).execute(any(Scenario.class));
        inOrder.verify(stepDefinition, never()).execute(any(Object[].class));
    }

    @Test
    public void aftersteps_are_executed_after_failed_step() throws Throwable {

        StepDefinition stepDefinition = mock(StepDefinition.class);
        doThrow(RuntimeException.class).when(stepDefinition).execute(any(Object[].class));
        HookDefinition afteStepHook = addAfterStepHook();

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));

        InOrder inOrder = inOrder(afteStepHook, stepDefinition);
        inOrder.verify(stepDefinition).execute(any(Object[].class));
        inOrder.verify(afteStepHook).execute(any(Scenario.class));
    }

    @Test
    public void aftersteps_executed_for_passed_step() throws Throwable {

        StepDefinition stepDefinition = mock(StepDefinition.class);
        HookDefinition afteStepHook1 = addAfterStepHook();
        HookDefinition afteStepHook2 = addAfterStepHook();

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));

        InOrder inOrder = inOrder(afteStepHook1, afteStepHook2, stepDefinition);
        inOrder.verify(stepDefinition).execute(any(Object[].class));
        inOrder.verify(afteStepHook1).execute(any(Scenario.class));
        inOrder.verify(afteStepHook2).execute(any(Scenario.class));
    }

    @Test
    public void hooks_execute_also_after_failure() throws Throwable {
        PickleStep step = mock(PickleStep.class);
        HookDefinition failingBeforeHook = addBeforeHook();
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(any(Scenario.class));
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();

        runner.runPickle(createPickleEventWithSteps(asList(step)));

        InOrder inOrder = inOrder(failingBeforeHook, beforeHook, afterHook);
        inOrder.verify(failingBeforeHook).execute(any(Scenario.class));
        inOrder.verify(beforeHook).execute(any(Scenario.class));
        inOrder.verify(afterHook).execute(any(Scenario.class));
    }

    @Test
    public void steps_are_executed() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));
        verify(stepDefinition).execute(any(Object[].class));
    }

    @Test
    public void steps_are_not_executed_on_dry_run() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        Runner dryRunner = createRunner(backend, "--dry-run");
        dryRunner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));
        verify(stepDefinition, never()).execute(any(Object[].class));
    }

    @Test
    public void hooks_not_executed_in_dry_run_mode() throws Throwable {
        Runner runner = createRunner(backend, "--dry-run");
        PickleStep step = mock(PickleStep.class);
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();
        HookDefinition afterStepHook = addAfterStepHook();

        runner.runPickle(createPickleEventWithSteps(asList(step)));

        verify(beforeHook, never()).execute(any(Scenario.class));
        verify(afterStepHook, never()).execute(any(Scenario.class));
        verify(afterHook, never()).execute(any(Scenario.class));
    }

    @Test
    public void hooks_not_executed_for_empty_pickles() throws Throwable {
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();
        HookDefinition afterStepHook = addAfterStepHook();

        runner.runPickle(createEmptyPickleEvent());

        verify(beforeHook, never()).execute(any(Scenario.class));
        verify(afterStepHook, never()).execute(any(Scenario.class));
        verify(afterHook, never()).execute(any(Scenario.class));
    }

    @Test
    public void backends_are_asked_for_snippets_for_undefined_steps() {
        PickleStep step = mock(PickleStep.class);
        runner.runPickle(createPickleEventWithSteps(asList(step)));

        verify(backend).getSnippet(ArgumentMatchers.eq(step), anyString(), any(FunctionNameGenerator.class));
    }

    private Runner createRunner(Backend backend) {
        return createRunner(backend, "");
    }

    private Runner createRunner(final Backend backend, String options) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(options);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(backend);
            }
        };
        EventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);
        return new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier).get();
    }

    private HookDefinition addBeforeHook() {
        return addHook(HookType.Before);
    }

    private HookDefinition addAfterHook() {
        return addHook(HookType.After);
    }

    private HookDefinition addAfterStepHook() {
        return addHook(HookType.AfterStep);
    }

    private HookDefinition addHook(HookType hookType) {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(PickleTag.class))).thenReturn(true);
        if (hookType == HookType.Before) {
            glue.addBeforeHook(hook);
        } else if (hookType == HookType.After){
            glue.addAfterHook(hook);
        } else if (hookType == HookType.AfterStep) {
            glue.addAfterStepHook(hook);
        }
        return hook;
    }

    private PickleEvent createEmptyPickleEvent() {
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, NO_STEPS, NO_TAGS, MOCK_LOCATIONS));
    }

    private PickleEvent createPickleEventMatchingStepDefinitions(List<StepDefinition> stepDefinitions) {
        List<PickleStep> steps = new ArrayList<PickleStep>(stepDefinitions.size());
        int i = 0;
        for (StepDefinition stepDefinition : stepDefinitions) {
            PickleStep step = mock(PickleStep.class);
            steps.add(step);
            when(stepDefinition.matchedArguments(step)).thenReturn(Collections.<Argument>emptyList());
            when(stepDefinition.getPattern()).thenReturn("pattern" + Integer.toString(++i));
            glue.addStepDefinition(stepDefinition);
        }
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, steps, NO_TAGS, MOCK_LOCATIONS));
    }

    private PickleEvent createPickleEventWithSteps(List<PickleStep> steps) {
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, steps, NO_TAGS, MOCK_LOCATIONS));
    }
}
