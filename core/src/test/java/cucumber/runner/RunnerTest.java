package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Scenario;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.messages.Messages.PickleTag;
import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.RuntimeGlueSupplier;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.ThreadLocalRunnerSupplier;
import cucumber.runtime.snippets.FunctionNameGenerator;
import io.cucumber.stepexpression.Argument;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cucumber.runtime.PickleHelper.pickle;
import static cucumber.runtime.PickleHelper.step;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RunnerTest {
    private final Backend backend = mock(Backend.class);
    private final Runner runner = createRunner(backend);

    @Test
    public void hooks_execute_when_world_exist() throws Throwable {
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();

        runner.runPickle(pickle(step()));

        InOrder inOrder = inOrder(beforeHook, afterHook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(beforeHook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(afterHook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend).disposeWorld();
    }

    @Test
    public void steps_are_skipped_after_failure() throws Throwable {
        HookDefinition failingBeforeHook = addBeforeHook();
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(ArgumentMatchers.<Scenario>any());
        StepDefinition stepDefinition = mock(StepDefinition.class);

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));

        InOrder inOrder = inOrder(failingBeforeHook, stepDefinition);
        inOrder.verify(failingBeforeHook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(stepDefinition, never()).execute(ArgumentMatchers.anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    public void aftersteps_are_executed_after_failed_step() throws Throwable {

        StepDefinition stepDefinition = mock(StepDefinition.class);
        doThrow(RuntimeException.class).when(stepDefinition).execute(ArgumentMatchers.anyString(), ArgumentMatchers.<Object[]>any());
        HookDefinition afteStepHook = addAfterStepHook();

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));

        InOrder inOrder = inOrder(afteStepHook, stepDefinition);
        inOrder.verify(stepDefinition).execute(ArgumentMatchers.anyString(), ArgumentMatchers.<Object[]>any());
        inOrder.verify(afteStepHook).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void aftersteps_executed_for_passed_step() throws Throwable {

        StepDefinition stepDefinition = mock(StepDefinition.class);
        HookDefinition afteStepHook1 = addAfterStepHook();
        HookDefinition afteStepHook2 = addAfterStepHook();

        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));

        InOrder inOrder = inOrder(afteStepHook1, afteStepHook2, stepDefinition);
        inOrder.verify(stepDefinition).execute(ArgumentMatchers.anyString(), ArgumentMatchers.<Object[]>any());
        inOrder.verify(afteStepHook1).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(afteStepHook2).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void hooks_execute_also_after_failure() throws Throwable {
        HookDefinition failingBeforeHook = addBeforeHook();
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(ArgumentMatchers.<Scenario>any());
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();

        runner.runPickle(pickle(step()));

        InOrder inOrder = inOrder(failingBeforeHook, beforeHook, afterHook);
        inOrder.verify(failingBeforeHook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(beforeHook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(afterHook).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void steps_are_executed() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        runner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));
        verify(stepDefinition).execute(ArgumentMatchers.anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    public void steps_are_not_executed_on_dry_run() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        Runner dryRunner = createRunner(backend, "--dry-run");
        dryRunner.runPickle(createPickleEventMatchingStepDefinitions(asList(stepDefinition)));
        verify(stepDefinition, never()).execute(ArgumentMatchers.anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    public void hooks_not_executed_in_dry_run_mode() throws Throwable {
        Runner runner = createRunner(backend, "--dry-run");
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();
        HookDefinition afterStepHook = addAfterStepHook();

        runner.runPickle(pickle(step()));

        verify(beforeHook, never()).execute(ArgumentMatchers.<Scenario>any());
        verify(afterStepHook, never()).execute(ArgumentMatchers.<Scenario>any());
        verify(afterHook, never()).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void hooks_not_executed_for_empty_pickles() throws Throwable {
        HookDefinition beforeHook = addBeforeHook();
        HookDefinition afterHook = addAfterHook();
        HookDefinition afterStepHook = addAfterStepHook();

        runner.runPickle(pickle());

        verify(beforeHook, never()).execute(ArgumentMatchers.<Scenario>any());
        verify(afterStepHook, never()).execute(ArgumentMatchers.<Scenario>any());
        verify(afterHook, never()).execute(ArgumentMatchers.<Scenario>any());
    }

    @Test
    public void backends_are_asked_for_snippets_for_undefined_steps() throws Throwable {
        PickleStep step = step();
        runner.runPickle(pickle(step));

        verify(backend).getSnippet(ArgumentMatchers.eq(step), ArgumentMatchers.anyString(), ArgumentMatchers.<FunctionNameGenerator>any());
    }

    private Runner createRunner(Backend backend) {
        return createRunner(backend, "-p null");
    }

    private Runner createRunner(final Backend backend, String options) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(options);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(backend);
            }
        };
        EventBus bus = new EventBus(TimeService.SYSTEM);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
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
            runner.getGlue().addBeforeHook(hook);
        } else if (hookType == HookType.After) {
            runner.getGlue().addAfterHook(hook);
        } else if (hookType == HookType.AfterStep) {
            runner.getGlue().addAfterStepHook(hook);
        }
        return hook;
    }

    private Pickle createPickleEventMatchingStepDefinitions(List<StepDefinition> stepDefinitions) {
        List<PickleStep> steps = new ArrayList<>(stepDefinitions.size());
        int i = 0;
        for (StepDefinition stepDefinition : stepDefinitions) {
            PickleStep step = step();
            steps.add(step);
            when(stepDefinition.matchedArguments(step)).thenReturn(Collections.<Argument>emptyList());
            when(stepDefinition.getPattern()).thenReturn("pattern" + Integer.toString(++i));
            runner.getGlue().addStepDefinition(stepDefinition);
        }
        return pickle(steps);
    }
}
