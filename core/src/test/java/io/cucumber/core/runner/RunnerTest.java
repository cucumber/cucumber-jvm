package io.cucumber.core.runner;

import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.snippets.TestSnippet;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.TypeRegistry;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());
    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    @Test
    public void hooks_execute_when_world_exist() throws Throwable {
        final HookDefinition beforeHook = addBeforeHook();
        final HookDefinition afterHook = addAfterHook();

        Backend backend = mock(Backend.class);
        when(backend.getSnippet()).thenReturn(new TestSnippet());
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Glue glue = invocation.getArgument(0);
                glue.addAfterHook(afterHook);
                glue.addBeforeHook(beforeHook);
                return null;
            }
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.<URI>anyList());


        PickleStep step = mock(PickleStep.class);
        when(step.getText()).thenReturn("some step");

        new Runner(bus, singletonList(backend), objectFactory, typeRegistry, runtimeOptions).runPickle(createPickleEventWithSteps(asList(step)));

        InOrder inOrder = inOrder(beforeHook, afterHook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(beforeHook).execute(any(Scenario.class));
        inOrder.verify(afterHook).execute(any(Scenario.class));
        inOrder.verify(backend).disposeWorld();
    }

    @Test
    public void steps_are_skipped_after_failure() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        PickleEvent pickleEventMatchingStepDefinitions = createPickleEventMatchingStepDefinitions(asList(stepDefinition));

        final HookDefinition failingBeforeHook = addBeforeHook();
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(ArgumentMatchers.<Scenario>any());
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(failingBeforeHook);
                glue.addStepDefinition(typeRegistry -> stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleEventMatchingStepDefinitions);

        InOrder inOrder = inOrder(failingBeforeHook, stepDefinition);
        inOrder.verify(failingBeforeHook).execute(any(Scenario.class));
        inOrder.verify(stepDefinition, never()).execute(any(Object[].class));
    }

    @Test
    public void aftersteps_are_executed_after_failed_step() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        doThrow(RuntimeException.class).when(stepDefinition).execute(ArgumentMatchers.<Object[]>any());
        PickleEvent pickleEventMatchingStepDefinitions = createPickleEventMatchingStepDefinitions(asList(stepDefinition));

        final HookDefinition afteStepHook = addAfterStepHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterHook(afteStepHook);
                glue.addStepDefinition(typeRegistry -> stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleEventMatchingStepDefinitions);

        InOrder inOrder = inOrder(afteStepHook, stepDefinition);
        inOrder.verify(stepDefinition).execute(any(Object[].class));
        inOrder.verify(afteStepHook).execute(any(Scenario.class));
    }

    @Test
    public void aftersteps_executed_for_passed_step() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        PickleEvent pickleEvent = createPickleEventMatchingStepDefinitions(asList(stepDefinition));

        final HookDefinition afteStepHook1 = addAfterStepHook();
        final HookDefinition afteStepHook2 = addAfterStepHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterHook(afteStepHook1);
                glue.addAfterHook(afteStepHook2);
                glue.addStepDefinition(typeRegistry -> stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleEvent);

        InOrder inOrder = inOrder(afteStepHook1, afteStepHook2, stepDefinition);
        inOrder.verify(stepDefinition).execute(any(Object[].class));
        inOrder.verify(afteStepHook1).execute(any(Scenario.class));
        inOrder.verify(afteStepHook2).execute(any(Scenario.class));
    }

    @Test
    public void hooks_execute_also_after_failure() throws Throwable {
        final HookDefinition failingBeforeHook = addBeforeHook();
        doThrow(RuntimeException.class).when(failingBeforeHook).execute(any(Scenario.class));
        final HookDefinition beforeHook = addBeforeHook();
        final HookDefinition afterHook = addAfterHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(failingBeforeHook);
                glue.addBeforeHook(beforeHook);
                glue.addAfterHook(afterHook);
            }
        };

        PickleStep step = mock(PickleStep.class);
        when(step.getText()).thenReturn("some step");
        runnerSupplier.get().runPickle(createPickleEventWithSteps(asList(step)));

        InOrder inOrder = inOrder(failingBeforeHook, beforeHook, afterHook);
        inOrder.verify(failingBeforeHook).execute(any(Scenario.class));
        inOrder.verify(beforeHook).execute(any(Scenario.class));
        inOrder.verify(afterHook).execute(any(Scenario.class));
    }

    @Test
    public void steps_are_executed() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        PickleEvent pickleEventMatchingStepDefinitions = createPickleEventMatchingStepDefinitions(asList(stepDefinition));
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(typeRegistry -> stepDefinition);
            }
        };
        runnerSupplier.get().runPickle(pickleEventMatchingStepDefinitions);
        verify(stepDefinition).execute(any(Object[].class));
    }

    @Test
    public void steps_are_not_executed_on_dry_run() throws Throwable {
        final StepDefinition stepDefinition = mock(StepDefinition.class);
        final PickleEvent pickleEvent = createPickleEventMatchingStepDefinitions(asList(stepDefinition));
        RuntimeOptions runtimeOptions = new RuntimeOptionsBuilder().setDryRun().build();
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(typeRegistry -> stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleEvent);
        verify(stepDefinition, never()).execute(any(Object[].class));
    }

    @Test
    public void hooks_not_executed_in_dry_run_mode() throws Throwable {
        RuntimeOptions runtimeOptions = new RuntimeOptionsBuilder().setDryRun().build();

        final HookDefinition beforeHook = addBeforeHook();
        final HookDefinition afterHook = addAfterHook();
        final HookDefinition afterStepHook = addAfterStepHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(beforeHook);
                glue.addBeforeHook(afterHook);
                glue.addAfterStepHook(afterStepHook);
            }
        };
        PickleStep step = mock(PickleStep.class);
        when(step.getText()).thenReturn("some step");

        runnerSupplier.get().runPickle(createPickleEventWithSteps(asList(step)));

        verify(beforeHook, never()).execute(any(Scenario.class));
        verify(afterStepHook, never()).execute(any(Scenario.class));
        verify(afterHook, never()).execute(any(Scenario.class));
    }

    @Test
    public void hooks_not_executed_for_empty_pickles() throws Throwable {
        final HookDefinition beforeHook = addBeforeHook();
        final HookDefinition afterHook = addAfterHook();
        final HookDefinition afterStepHook = addAfterStepHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, typeRegistry, runtimeOptions) {

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(beforeHook);
                glue.addBeforeHook(afterHook);
                glue.addAfterStepHook(afterStepHook);
            }
        };

        runnerSupplier.get().runPickle(createEmptyPickleEvent());

        verify(beforeHook, never()).execute(any(Scenario.class));
        verify(afterStepHook, never()).execute(any(Scenario.class));
        verify(afterHook, never()).execute(any(Scenario.class));
    }

    @Test
    public void backends_are_asked_for_snippets_for_undefined_steps() {
        PickleStep step = mock(PickleStep.class);
        when(step.getText()).thenReturn("some step");
        Backend backend = mock(Backend.class);
        when(backend.getSnippet()).thenReturn(new TestSnippet());
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        Runner runner = new Runner(bus, singletonList(backend), objectFactory, typeRegistry, runtimeOptions);
        runner.runPickle(createPickleEventWithSteps(asList(step)));
        verify(backend).getSnippet();
    }

    private HookDefinition addBeforeHook() {
        return addHook();
    }

    private HookDefinition addAfterHook() {
        return addHook();
    }

    private HookDefinition addAfterStepHook() {
        return addHook();
    }

    private HookDefinition addHook() {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(ArgumentMatchers.<PickleTag>anyList())).thenReturn(true);
        return hook;
    }

    private PickleEvent createEmptyPickleEvent() {
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, NO_STEPS, NO_TAGS, MOCK_LOCATIONS));
    }

    private PickleEvent createPickleEventMatchingStepDefinitions(List<StepDefinition> stepDefinitions) {
        List<PickleStep> steps = new ArrayList<>(stepDefinitions.size());
        int i = 0;
        for (StepDefinition stepDefinition : stepDefinitions) {
            PickleStep step = mock(PickleStep.class);
            steps.add(step);
            when(stepDefinition.matchedArguments(step)).thenReturn(Collections.<Argument>emptyList());
            when(stepDefinition.getPattern()).thenReturn("pattern" + Integer.toString(++i));
        }
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, steps, NO_TAGS, MOCK_LOCATIONS));
    }

    private PickleEvent createPickleEventWithSteps(List<PickleStep> steps) {
        return new PickleEvent("uri", new Pickle(NAME, ENGLISH, steps, NO_TAGS, MOCK_LOCATIONS));
    }
}
