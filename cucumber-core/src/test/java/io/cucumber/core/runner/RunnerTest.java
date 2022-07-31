package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.snippets.TestSnippet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunnerTest {

    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    @Test
    void hooks_execute_inside_world_and_around_world() {
        StaticHookDefinition beforeAllHook = createStaticHook();
        StaticHookDefinition afterAllHook = createStaticHook();
        HookDefinition beforeHook = createHook();
        HookDefinition afterHook = createHook();

        Backend backend = mock(Backend.class);
        when(backend.getSnippet()).thenReturn(new TestSnippet());
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        doAnswer(invocation -> {
            Glue glue = invocation.getArgument(0);
            glue.addBeforeAllHook(beforeAllHook);
            glue.addAfterAllHook(afterAllHook);
            glue.addBeforeHook(beforeHook);
            glue.addAfterHook(afterHook);
            return null;
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.anyList());

        Runner runner = new Runner(bus, singletonList(backend), objectFactory, runtimeOptions);
        runner.runBeforeAllHooks();
        runner.runPickle(createPicklesWithSteps());
        runner.runAfterAllHooks();

        InOrder inOrder = inOrder(beforeAllHook, afterAllHook, beforeHook, afterHook, backend);
        inOrder.verify(beforeAllHook).execute();
        inOrder.verify(backend).buildWorld();
        inOrder.verify(beforeHook).execute(any(TestCaseState.class));
        inOrder.verify(afterHook).execute(any(TestCaseState.class));
        inOrder.verify(backend).disposeWorld();
        inOrder.verify(afterAllHook).execute();
    }

    private Pickle createPicklesWithSteps() {
        Feature feature = TestFeatureParser.parse("file:path/to.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given some step\n");
        return feature.getPickles().get(0);
    }

    private StaticHookDefinition createStaticHook() {
        StaticHookDefinition hook = mock(StaticHookDefinition.class);
        when(hook.getLocation()).thenReturn("");
        return hook;
    }

    private HookDefinition createHook() {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.getTagExpression()).thenReturn("");
        when(hook.getLocation()).thenReturn("");
        return hook;
    }

    @Test
    void steps_are_skipped_after_failure() {
        StubStepDefinition stepDefinition = spy(new StubStepDefinition("some step"));
        Pickle pickleMatchingStepDefinitions = createPickleMatchingStepDefinitions(stepDefinition);

        final HookDefinition failingBeforeHook = createHook();
        doThrow(new RuntimeException("Boom")).when(failingBeforeHook).execute(ArgumentMatchers.any());
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(failingBeforeHook);
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleMatchingStepDefinitions);

        InOrder inOrder = inOrder(failingBeforeHook, stepDefinition);
        inOrder.verify(failingBeforeHook).execute(any(TestCaseState.class));
        inOrder.verify(stepDefinition, never()).execute(any(Object[].class));
    }

    private Pickle createPickleMatchingStepDefinitions(StubStepDefinition stepDefinition) {
        String pattern = stepDefinition.getPattern();
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given " + pattern + "\n");
        return feature.getPickles().get(0);
    }

    @Test
    void aftersteps_are_executed_after_failed_step() {
        StubStepDefinition stepDefinition = spy(new StubStepDefinition("some step") {

            @Override
            public void execute(Object[] args) {
                super.execute(args);
                throw new RuntimeException();
            }
        });

        Pickle pickleMatchingStepDefinitions = createPickleMatchingStepDefinitions(stepDefinition);

        final HookDefinition afterStepHook = createHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterHook(afterStepHook);
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleMatchingStepDefinitions);

        InOrder inOrder = inOrder(afterStepHook, stepDefinition);
        inOrder.verify(stepDefinition).execute(any(Object[].class));
        inOrder.verify(afterStepHook).execute(any(TestCaseState.class));
    }

    @Test
    void aftersteps_executed_for_passed_step() {
        StubStepDefinition stepDefinition = spy(new StubStepDefinition("some step"));
        Pickle pickle = createPickleMatchingStepDefinitions(stepDefinition);

        HookDefinition afteStepHook1 = createHook();
        HookDefinition afteStepHook2 = createHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterHook(afteStepHook1);
                glue.addAfterHook(afteStepHook2);
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickle);

        InOrder inOrder = inOrder(afteStepHook1, afteStepHook2, stepDefinition);
        inOrder.verify(stepDefinition).execute(any(Object[].class));
        inOrder.verify(afteStepHook2).execute(any(TestCaseState.class));
        inOrder.verify(afteStepHook1).execute(any(TestCaseState.class));
    }

    @Test
    void hooks_execute_also_after_failure() {
        HookDefinition beforeHook = createHook();
        HookDefinition afterHook = createHook();

        HookDefinition failingBeforeHook = createHook();
        doThrow(new RuntimeException("boom")).when(failingBeforeHook).execute(any(TestCaseState.class));

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(failingBeforeHook);
                glue.addBeforeHook(beforeHook);
                glue.addAfterHook(afterHook);
            }
        };

        runnerSupplier.get().runPickle(createPicklesWithSteps());

        InOrder inOrder = inOrder(failingBeforeHook, beforeHook, afterHook);
        inOrder.verify(failingBeforeHook).execute(any(TestCaseState.class));
        inOrder.verify(beforeHook).execute(any(TestCaseState.class));
        inOrder.verify(afterHook).execute(any(TestCaseState.class));
    }

    @Test
    void all_static_hooks_execute_also_after_failure() {
        StaticHookDefinition beforeAllHook = createStaticHook();
        StaticHookDefinition failingBeforeAllHook = createStaticHook();
        doThrow(new RuntimeException("boom")).when(failingBeforeAllHook).execute();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeAllHook(beforeAllHook);
                glue.addBeforeAllHook(failingBeforeAllHook);
            }
        };

        Runner runner = runnerSupplier.get();
        assertThrows(RuntimeException.class, runner::runBeforeAllHooks);

        InOrder inOrder = inOrder(beforeAllHook, failingBeforeAllHook);
        inOrder.verify(beforeAllHook).execute();
        inOrder.verify(failingBeforeAllHook).execute();
    }

    @Test
    void steps_are_executed() {
        StubStepDefinition stepDefinition = new StubStepDefinition("some step");
        Pickle pickleMatchingStepDefinitions = createPickleMatchingStepDefinitions(stepDefinition);
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);
            }
        };
        runnerSupplier.get().runPickle(pickleMatchingStepDefinitions);
        assertThat(stepDefinition.getArgs(), is(equalTo(emptyList())));
    }

    @Test
    void steps_are_not_executed_on_dry_run() {
        StubStepDefinition stepDefinition = new StubStepDefinition("some step");
        Pickle pickle = createPickleMatchingStepDefinitions(stepDefinition);
        RuntimeOptions runtimeOptions = new RuntimeOptionsBuilder().setDryRun().build();
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickle);
        assertThat(stepDefinition.getArgs(), is(nullValue()));
    }

    @Test
    void hooks_not_executed_in_dry_run_mode() {
        RuntimeOptions runtimeOptions = new RuntimeOptionsBuilder().setDryRun().build();

        StaticHookDefinition beforeAllHook = createStaticHook();
        StaticHookDefinition afterAllHook = createStaticHook();
        HookDefinition beforeHook = createHook();
        HookDefinition afterHook = createHook();
        HookDefinition beforeStepHook = createHook();
        HookDefinition afterStepHook = createHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeAllHook(beforeAllHook);
                glue.addAfterAllHook(afterAllHook);
                glue.addBeforeHook(beforeHook);
                glue.addAfterHook(afterHook);
                glue.addBeforeStepHook(beforeStepHook);
                glue.addAfterStepHook(afterStepHook);
            }
        };
        runnerSupplier.get().runBeforeAllHooks();
        runnerSupplier.get().runPickle(createPicklesWithSteps());
        runnerSupplier.get().runAfterAllHooks();

        verify(beforeAllHook, never()).execute();
        verify(afterAllHook, never()).execute();
        verify(beforeHook, never()).execute(any(TestCaseState.class));
        verify(afterHook, never()).execute(any(TestCaseState.class));
        verify(beforeStepHook, never()).execute(any(TestCaseState.class));
        verify(afterStepHook, never()).execute(any(TestCaseState.class));
    }

    @Test
    void scenario_hooks_not_executed_for_empty_pickles() {
        HookDefinition beforeHook = createHook();
        HookDefinition afterHook = createHook();
        HookDefinition beforeStepHook = createHook();
        HookDefinition afterStepHook = createHook();

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(beforeHook);
                glue.addAfterHook(afterHook);
                glue.addBeforeStepHook(beforeStepHook);
                glue.addAfterStepHook(afterStepHook);
            }
        };

        runnerSupplier.get().runPickle(createEmptyPickle());

        verify(beforeHook, never()).execute(any(TestCaseState.class));
        verify(afterStepHook, never()).execute(any(TestCaseState.class));
        verify(afterHook, never()).execute(any(TestCaseState.class));
    }

    private Pickle createEmptyPickle() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n");
        return feature.getPickles().get(0);
    }

    @Test
    void backends_are_asked_for_snippets_for_undefined_steps() {
        Backend backend = mock(Backend.class);
        when(backend.getSnippet()).thenReturn(new TestSnippet());
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        Runner runner = new Runner(bus, singletonList(backend), objectFactory, runtimeOptions);
        runner.runPickle(createPicklesWithSteps());
        verify(backend).getSnippet();
    }

}
