package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Snippet;
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

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunnerTest {

    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    @Test
    void hooks_execute_inside_world_and_around_world() {
        List<String> listener = new ArrayList<>();
        StaticHookDefinition beforeAllHook = new MockStaticHookDefinition("beforeAllHook", listener);
        StaticHookDefinition afterAllHook = new MockStaticHookDefinition("afterAllHook", listener);
        HookDefinition beforeHook = new MockHookDefinition("beforeHook", listener);
        HookDefinition afterHook = new MockHookDefinition("afterHook", listener);

        Backend backend = new MockBackend(beforeAllHook, afterAllHook, beforeHook, afterHook, listener);
        ObjectFactory objectFactory = new MockObjectFactory();

        Runner runner = new Runner(bus, singletonList(backend), objectFactory, runtimeOptions);
        runner.runBeforeAllHooks();
        runner.runPickle(createPicklesWithSteps());
        runner.runAfterAllHooks();

        assertLinesMatch(
            List.of("beforeAllHook", "buildWorld", "beforeHook", "afterHook", "disposeWorld", "afterAllHook"),
            listener);
    }

    private Pickle createPicklesWithSteps() {
        Feature feature = TestFeatureParser.parse("file:path/to.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given some step\n");
        return feature.getPickles().get(0);
    }

    @Test
    void steps_are_skipped_after_failure() {
        List<String> listener = new ArrayList<>();
        StubStepDefinition stepDefinition = new MockStubStepDefinition(listener);
        Pickle pickleMatchingStepDefinitions = createPickleMatchingStepDefinitions(stepDefinition);

        final HookDefinition failingBeforeHook = new MockHookDefinition("beforeHook", listener,
            new RuntimeException("Boom"));
        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(failingBeforeHook);
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleMatchingStepDefinitions);

        assertLinesMatch(List.of("beforeHook"), listener);
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
        List<String> listener = new ArrayList<>();
        StubStepDefinition stepDefinition = new MockStubStepDefinition(listener, new RuntimeException());
        Pickle pickleMatchingStepDefinitions = createPickleMatchingStepDefinitions(stepDefinition);

        final HookDefinition afterStepHook = new MockHookDefinition("afterHook", listener);

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterHook(afterStepHook);
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickleMatchingStepDefinitions);

        assertLinesMatch(List.of("stepDefinition", "afterHook"), listener);
    }

    @Test
    void aftersteps_executed_for_passed_step() {
        List<String> listener = new ArrayList<>();
        StubStepDefinition stepDefinition = new MockStubStepDefinition(listener);
        Pickle pickle = createPickleMatchingStepDefinitions(stepDefinition);

        HookDefinition afteStepHook1 = new MockHookDefinition("afterHook1", listener);
        HookDefinition afteStepHook2 = new MockHookDefinition("afterHook2", listener);

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterHook(afteStepHook1);
                glue.addAfterHook(afteStepHook2);
                glue.addStepDefinition(stepDefinition);
            }
        };

        runnerSupplier.get().runPickle(pickle);

        assertLinesMatch(List.of("stepDefinition", "afterHook2", "afterHook1"), listener);
    }

    @Test
    void hooks_execute_also_after_failure() {
        List<String> listener = new ArrayList<>();
        HookDefinition beforeHook = new MockHookDefinition("beforeHook", listener);
        HookDefinition afterHook = new MockHookDefinition("afterHook", listener);
        ;

        HookDefinition failingBeforeHook = new MockHookDefinition("failingBeforeHook", listener,
            new RuntimeException("boom"));

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeHook(failingBeforeHook);
                glue.addBeforeHook(beforeHook);
                glue.addAfterHook(afterHook);
            }
        };

        runnerSupplier.get().runPickle(createPicklesWithSteps());

        assertLinesMatch(List.of("failingBeforeHook", "beforeHook", "afterHook"), listener);
    }

    @Test
    void all_static_hooks_execute_also_after_failure() {
        List<String> listener = new ArrayList<>();
        StaticHookDefinition beforeAllHook = new MockStaticHookDefinition("beforeAllHook", listener);
        StaticHookDefinition failingBeforeAllHook = new MockStaticHookDefinition("failingBeforeAllHook", listener,
            new RuntimeException("boom"));

        TestRunnerSupplier runnerSupplier = new TestRunnerSupplier(bus, runtimeOptions) {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addBeforeAllHook(beforeAllHook);
                glue.addBeforeAllHook(failingBeforeAllHook);
            }
        };

        Runner runner = runnerSupplier.get();
        assertThrows(RuntimeException.class, runner::runBeforeAllHooks);

        assertLinesMatch(List.of("beforeAllHook", "failingBeforeAllHook"), listener);
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

        List<String> listener = new ArrayList<>();
        StaticHookDefinition beforeAllHook = new MockStaticHookDefinition("beforeAllHook", listener);
        StaticHookDefinition afterAllHook = new MockStaticHookDefinition("afterAllHook", listener);
        HookDefinition beforeHook = new MockHookDefinition("beforeHook", listener);
        HookDefinition afterHook = new MockHookDefinition("afterHook", listener);
        HookDefinition beforeStepHook = new MockHookDefinition("beforeStepHook", listener);
        HookDefinition afterStepHook = new MockHookDefinition("afterStepHook", listener);

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

        assertLinesMatch(Collections.emptyList(), listener);
    }

    @Test
    void scenario_hooks_not_executed_for_empty_pickles() {
        List<String> listener = new ArrayList<>();
        HookDefinition beforeHook = new MockHookDefinition("beforeHook", listener);
        HookDefinition afterHook = new MockHookDefinition("afterHook", listener);
        HookDefinition beforeStepHook = new MockHookDefinition("beforeStepHook", listener);
        HookDefinition afterStepHook = new MockHookDefinition("afterStepHook", listener);

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

        assertLinesMatch(Collections.emptyList(), listener);
    }

    private Pickle createEmptyPickle() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n");
        return feature.getPickles().get(0);
    }

    @Test
    void backends_are_asked_for_snippets_for_undefined_steps() {
        List<String> listener = new ArrayList<>();
        MockBackend backend = new MockBackend(null, null, null, null, listener);
        ObjectFactory objectFactory = new MockObjectFactory();
        Runner runner = new Runner(bus, singletonList(backend), objectFactory, runtimeOptions);
        runner.runPickle(createPicklesWithSteps());
        assertTrue(backend.getSnippetCalled);
    }

    private static class MockBackend implements Backend {
        private final StaticHookDefinition beforeAllHook;
        private final StaticHookDefinition afterAllHook;
        private final HookDefinition beforeHook;
        private final HookDefinition afterHook;
        private final List<String> listener;
        boolean getSnippetCalled;

        public MockBackend(
                StaticHookDefinition beforeAllHook, StaticHookDefinition afterAllHook,
                HookDefinition beforeHook, HookDefinition afterHook, List<String> listener
        ) {
            this.beforeAllHook = beforeAllHook;
            this.afterAllHook = afterAllHook;
            this.beforeHook = beforeHook;
            this.afterHook = afterHook;
            this.listener = listener;
        }

        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
            if (beforeAllHook != null) {
                glue.addBeforeAllHook(beforeAllHook);
            }
            if (afterAllHook != null) {
                glue.addAfterAllHook(afterAllHook);
            }
            if (beforeHook != null) {
                glue.addBeforeHook(beforeHook);
            }
            if (afterHook != null) {
                glue.addAfterHook(afterHook);
            }
        }

        @Override
        public void buildWorld() {
            listener.add("buildWorld");
        }

        @Override
        public void disposeWorld() {
            listener.add("disposeWorld");
        }

        @Override
        public Snippet getSnippet() {
            getSnippetCalled = true;
            return new TestSnippet();
        }
    }

    private static class MockObjectFactory implements ObjectFactory {
        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }

    private static class MockStaticHookDefinition implements StaticHookDefinition {
        private final String hookName;
        private final List<String> listener;
        private final RuntimeException exception;

        public MockStaticHookDefinition(String hookName, List<String> listener) {
            this(hookName, listener, null);
        }

        public MockStaticHookDefinition(String hookName, List<String> listener, RuntimeException exception) {
            this.hookName = hookName;
            this.listener = listener;
            this.exception = exception;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "";
        }

        @Override
        public void execute() {
            listener.add(hookName);
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    private static class MockHookDefinition implements HookDefinition {
        private final String hookName;
        private final List<String> listener;
        private final RuntimeException exception;

        public MockHookDefinition(String hookName, List<String> listener) {
            this(hookName, listener, null);
        }

        public MockHookDefinition(String hookName, List<String> listener, RuntimeException exception) {
            this.hookName = hookName;
            this.listener = listener;
            this.exception = exception;
        }

        @Override
        public void execute(io.cucumber.core.backend.TestCaseState state) {
            listener.add(hookName);
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "";
        }
    }

    private static class MockStubStepDefinition extends StubStepDefinition {
        private final List<String> listener;
        private final RuntimeException exception;

        MockStubStepDefinition(List<String> listener) {
            this(listener, null);
        }

        MockStubStepDefinition(List<String> listener, RuntimeException exception) {
            super("some step");
            this.listener = listener;
            this.exception = exception;
        }

        @Override
        public void execute(Object[] args) {
            super.execute(args);
            listener.add("stepDefinition");
            if (exception != null) {
                throw exception;
            }
        }
    }
}
