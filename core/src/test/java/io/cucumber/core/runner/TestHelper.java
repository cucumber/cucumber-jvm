package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Located;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.FeatureSupplier;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TestFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.datatable.DataTable;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import org.mockito.stubbing.Answer;
import org.opentest4j.TestAbortedException;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static io.cucumber.plugin.event.Status.FAILED;
import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.PENDING;
import static io.cucumber.plugin.event.Status.SKIPPED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static java.time.Duration.ZERO;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestHelper {

    private List<Feature> features = Collections.emptyList();
    private Map<String, Result> stepsToResult = Collections.emptyMap();
    private Map<String, String> stepsToLocation = Collections.emptyMap();
    private List<SimpleEntry<String, Result>> hooks = Collections.emptyList();
    private List<String> hookLocations = Collections.emptyList();
    private List<Answer<Object>> hookActions = Collections.emptyList();
    private TimeServiceType timeServiceType = TimeServiceType.FIXED_INCREMENT_ON_STEP_START;
    private Duration timeServiceIncrement = Duration.ZERO;
    private Object formatterUnderTest = null;
    private List<String> runtimeArgs = Collections.emptyList();

    private TestHelper() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Result result(String status) {
        return result(fromLowerCaseName(status));
    }

    public static Result result(String status, Throwable error) {
        return result(fromLowerCaseName(status), error);
    }

    private static Status fromLowerCaseName(String lowerCaseName) {
        return Status.valueOf(lowerCaseName.toUpperCase(ROOT));
    }

    public static Result result(Status status) {
        switch (status) {
            case FAILED:
                return result(status, mockAssertionFailedError());
            case AMBIGUOUS:
                return result(status, mockAmbiguousStepDefinitionException());
            case PENDING:
                return result(status, new TestPendingException());
            default:
                return result(status, null);
        }
    }

    public static Result result(Status status, Throwable error) {
        return new Result(status, Duration.ZERO, error);
    }

    public static Answer<Object> createWriteHookAction(final String output) {
        return invocation -> {
            TestCaseState state = (TestCaseState) invocation.getArguments()[0];
            state.write(output);
            return null;
        };
    }

    public static Answer<Object> createEmbedHookAction(final byte[] data, final String mediaType) {
        return createEmbedHookAction(data, mediaType, null);
    }

    @SuppressWarnings("deprecation")
    public static Answer<Object> createEmbedHookAction(final byte[] data, final String mediaType, final String name) {
        return invocation -> {
            TestCaseState state = (TestCaseState) invocation.getArguments()[0];
            if (name != null) {
                state.embed(data, mediaType, name);
            } else {
                state.embed(data, mediaType);
            }
            return null;
        };
    }

    private static TestAbortedException mockAssertionFailedError() {
        class MockedTestAbortedException extends TestAbortedException {
            MockedTestAbortedException() {
                super("the message");
            }

            @Override
            public void printStackTrace(PrintStream s) {
                s.print("the stack trace");
            }

            @Override
            public void printStackTrace(PrintWriter s) {
                s.print("the stack trace");
            }
        }
        return new MockedTestAbortedException();
    }

    private static AmbiguousStepDefinitionsException mockAmbiguousStepDefinitionException() {
        AmbiguousStepDefinitionsException exception = mock(AmbiguousStepDefinitionsException.class);
        Answer<Object> printStackTraceHandler = invocation -> {
            PrintWriter writer = (PrintWriter) invocation.getArguments()[0];
            writer.print("the stack trace");
            return null;
        };
        doAnswer(printStackTraceHandler).when(exception).printStackTrace((PrintWriter) any());
        when(exception.getMessage()).thenReturn("the message");
        return exception;
    }

    public static SimpleEntry<String, Result> hookEntry(String type, Result result) {
        return new SimpleEntry<>(type, result);
    }

    public void run() {

        final Supplier<ClassLoader> classLoader = TestHelper.class::getClassLoader;

        final BackendSupplier backendSupplier = new TestHelperBackendSupplier(
            features,
            stepsToResult,
            stepsToLocation,
            hooks,
            hookLocations,
            hookActions
        );

        final EventBus bus = createEventBus();

        final FeatureSupplier featureSupplier = features.isEmpty()
            ? null // assume feature paths passed in as args instead
            : new TestFeatureSupplier(bus, features);

        Runtime.Builder runtimeBuilder = Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(runtimeArgs)
                    .build()
            )
            .withClassLoader(classLoader)
            .withBackendSupplier(backendSupplier)
            .withFeatureSupplier(featureSupplier)
            .withEventBus(bus);

        if (formatterUnderTest instanceof ConcurrentEventListener) {
            ((ConcurrentEventListener) formatterUnderTest).setEventPublisher(bus);
        } else if (formatterUnderTest instanceof EventListener) {
            ((EventListener) formatterUnderTest).setEventPublisher(bus);
        } else if (formatterUnderTest instanceof Plugin) {
            runtimeBuilder.withAdditionalPlugins((Plugin) formatterUnderTest);
        }

        runtimeBuilder.build().run();
    }

    private EventBus createEventBus() {
        EventBus bus = null;

        if (TimeServiceType.REAL_TIME.equals(this.timeServiceType)) {
            bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        } else if (TimeServiceType.FIXED_INCREMENT_ON_STEP_START.equals(this.timeServiceType)) {
            final StepDurationTimeService timeService = new StepDurationTimeService(this.timeServiceIncrement);
            bus = new TimeServiceEventBus(timeService, UUID::randomUUID);
            timeService.setEventPublisher(bus);
        } else if (TimeServiceType.FIXED_INCREMENT.equals(this.timeServiceType)) {
            bus = new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")), UUID::randomUUID);
        }
        return bus;
    }

    public enum TimeServiceType {
        REAL_TIME, FIXED_INCREMENT, FIXED_INCREMENT_ON_STEP_START
    }

    public static final class TestHelperBackendSupplier extends TestBackendSupplier {

        private final List<Feature> features;
        private final Map<String, Result> stepsToResult;
        private final Map<String, String> stepsToLocation;
        private final List<SimpleEntry<String, Result>> hooks;
        private final List<String> hookLocations;
        private final List<Answer<Object>> hookActions;

        TestHelperBackendSupplier(List<Feature> features, Map<String, Result> stepsToResult, Map<String, String> stepsToLocation, List<SimpleEntry<String, Result>> hooks, List<String> hookLocations, List<Answer<Object>> hookActions) {
            this.features = features;
            this.stepsToResult = stepsToResult;
            this.stepsToLocation = stepsToLocation;
            this.hooks = hooks;
            this.hookLocations = hookLocations;
            this.hookActions = hookActions;
        }

        public TestHelperBackendSupplier(List<Feature> features) {
            this(
                features,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
            );
        }

        private static void mockSteps(Glue glue, List<Feature> features,
                                      Map<String, Result> stepsToResult,
                                      final Map<String, String> stepsToLocation) {
            List<Step> steps = new ArrayList<>();
            for (Feature feature : features) {
                for (Pickle pickle : feature.getPickles()) {
                    for (Step step : pickle.getSteps()) {
                        if (!containsStep(steps, step)) {
                            steps.add(step);
                        }
                    }
                }
            }

            for (final Step step : steps) {
                final Result stepResult = getResultWithDefaultPassed(stepsToResult, step.getText());
                if (stepResult.getStatus().is(UNDEFINED)) {
                    continue;
                }

                Type[] types = mapArgumentToTypes(step);
                Located located = new Located() {
                    @Override
                    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                        return false;
                    }

                    @Override
                    public String getLocation() {
                        return "stubbed test helper location";
                    }
                };

                StepDefinition stepDefinition = new StubStepDefinition(step.getText(), types) {

                    @Override
                    public void execute(Object[] args) {
                        super.execute(args);
                        if (stepResult.getStatus().is(PENDING)) {
                            throw new TestPendingException();
                        } else if (stepResult.getStatus().is(FAILED)) {
                            throw new CucumberInvocationTargetException(located, new InvocationTargetException(stepResult.getError()));
                        } else if (stepResult.getStatus().is(SKIPPED) && (stepResult.getError() != null)) {
                            throw new CucumberInvocationTargetException(located, new InvocationTargetException(stepResult.getError()));
                        } else if (!stepResult.getStatus().is(PASSED) && !stepResult.getStatus().is(SKIPPED)) {
                            fail("Cannot mock step to the result: " + stepResult.getStatus());
                        }
                    }

                    @Override
                    public String getLocation() {
                        return stepsToLocation.getOrDefault(step.getText(), "mocked location");
                    }
                };

                glue.addStepDefinition(stepDefinition);
            }
        }

        private static Result getResultWithDefaultPassed(Map<String, Result> stepsToResult, String step) {
            return stepsToResult.containsKey(step) ? stepsToResult.get(step) : new Result(PASSED, ZERO, null);
        }

        private static boolean containsStep(List<Step> steps, Step step) {
            for (Step definedSteps : steps) {
                if (definedSteps.getText().equals(step.getText())
                    && (definedSteps.getArgument() == null) == (step.getArgument() == null)) {
                    return true;
                }
            }

            return false;
        }

        private static Type[] mapArgumentToTypes(Step step) {
            Type[] types = new Type[0];
            Argument argument = step.getArgument();
            if (argument == null) {
                return types;
            } else if (argument instanceof DocStringArgument) {
                types = new Type[]{String.class};
            } else if (argument instanceof DataTableArgument) {
                types = new Type[]{DataTable.class};
            }
            return types;
        }

        private static void mockHooks(Glue glue, final List<SimpleEntry<String, Result>> hooks,
                                      final List<String> hookLocations,
                                      final List<Answer<Object>> hookActions) {
            List<HookDefinition> beforeHooks = new ArrayList<>();
            List<HookDefinition> afterHooks = new ArrayList<>();
            List<HookDefinition> beforeStepHooks = new ArrayList<>();
            List<HookDefinition> afterStepHooks = new ArrayList<>();
            for (int i = 0; i < hooks.size(); ++i) {
                String hookLocation = hookLocations.size() > i ? hookLocations.get(i) : null;
                Answer<Object> hookAction = hookActions.size() > i ? hookActions.get(i) : null;
                mockHook(hooks.get(i), hookLocation, hookAction, beforeHooks, afterHooks, beforeStepHooks, afterStepHooks);
            }
            for (HookDefinition hook : beforeHooks) {
                glue.addBeforeHook(hook);
            }
            for (HookDefinition hook : afterHooks) {
                glue.addAfterHook(hook);
            }
            for (HookDefinition hook : beforeStepHooks) {
                glue.addBeforeStepHook(hook);
            }
            for (HookDefinition hook : afterStepHooks) {
                glue.addAfterStepHook(hook);
            }
        }

        private static void mockHook(final SimpleEntry<String, Result> hookEntry,
                                     final String hookLocation,
                                     final Answer<Object> action,
                                     final List<HookDefinition> beforeHooks,
                                     final List<HookDefinition> afterHooks,
                                     final List<HookDefinition> beforeStepHooks,
                                     final List<HookDefinition> afterStepHooks) {
            HookDefinition hook = mock(HookDefinition.class);
            when(hook.getTagExpression()).thenReturn("");
            if (hookLocation != null) {
                when(hook.getLocation()).thenReturn(hookLocation);
            }
            if (action != null) {
                doAnswer(action).when(hook).execute(any());
            }
            Located located = new Located() {
                @Override
                public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                    return false;
                }

                @Override
                public String getLocation() {
                    return "test helper mocked location";
                }
            };
            if (hookEntry.getValue().getStatus().is(FAILED)) {
                Throwable error = hookEntry.getValue().getError();
                CucumberInvocationTargetException exception = new CucumberInvocationTargetException(located, new InvocationTargetException(error));
                doThrow(exception).when(hook).execute(any());
            } else if (hookEntry.getValue().getStatus().is(PENDING)) {
                TestPendingException testPendingException = new TestPendingException();
                CucumberInvocationTargetException exception = new CucumberInvocationTargetException(located, new InvocationTargetException(testPendingException));
                doThrow(exception).when(hook).execute(any());
            }
            if ("before".equals(hookEntry.getKey())) {
                beforeHooks.add(hook);
            } else if ("after".equals(hookEntry.getKey())) {
                afterHooks.add(hook);
            } else if ("afterstep".equals(hookEntry.getKey())) {
                afterStepHooks.add(hook);
            } else if ("beforestep".equals(hookEntry.getKey())) {
                beforeStepHooks.add(hook);
            } else {
                fail("Only before, after and afterstep hooks are allowed, hook type found was: " + hookEntry.getKey());
            }
        }

        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
            mockSteps(glue, features, stepsToResult, stepsToLocation);
            mockHooks(glue, hooks, hookLocations, hookActions);
        }

    }

    public static final class Builder {
        private final TestHelper instance = new TestHelper();

        private Builder() {
        }

        public Builder withFeatures(Feature... features) {
            return withFeatures(Arrays.asList(features));
        }

        public Builder withFeatures(List<Feature> features) {
            this.instance.features = features;
            return this;
        }

        public Builder withStepsToResult(Map<String, Result> stepsToResult) {
            this.instance.stepsToResult = stepsToResult;
            return this;
        }

        public Builder withStepsToLocation(Map<String, String> stepsToLocation) {
            this.instance.stepsToLocation = stepsToLocation;
            return this;
        }

        public Builder withHooks(List<SimpleEntry<String, Result>> hooks) {
            this.instance.hooks = hooks;
            return this;
        }

        public Builder withHookLocations(List<String> hookLocations) {
            this.instance.hookLocations = hookLocations;
            return this;
        }

        public Builder withHookActions(List<Answer<Object>> hookActions) {
            this.instance.hookActions = hookActions;
            return this;
        }

        /**
         * Set what the time increment should be when using {@link TimeServiceType#FIXED_INCREMENT}
         * or {@link TimeServiceType#FIXED_INCREMENT_ON_STEP_START}
         *
         * @param timeServiceIncrement increment to be used
         * @return this instance
         */
        public Builder withTimeServiceIncrement(Duration timeServiceIncrement) {
            this.instance.timeServiceIncrement = timeServiceIncrement;
            return this;
        }

        /**
         * Specifies what type of TimeService to be used by the {@link EventBus}
         * {@link TimeServiceType#REAL_TIME} > {@link Clock#systemUTC()}
         * {@link TimeServiceType#FIXED_INCREMENT} > {@link ClockStub}
         * {@link TimeServiceType#FIXED_INCREMENT_ON_STEP_START} > {@link StepDurationTimeService}
         * <p>
         * Defaults to {@link TimeServiceType#FIXED_INCREMENT_ON_STEP_START}
         * <p>
         * Note: when running tests with multiple threads & not using {@link TimeServiceType#REAL_TIME}
         * it can inadvertently affect the order of {@link Event}s
         * published to any {@link ConcurrentEventListener}s used during the test run
         *
         * @return this instance
         */
        public Builder withTimeServiceType(TimeServiceType timeServiceType) {
            this.instance.timeServiceType = timeServiceType;
            return this;
        }

        /**
         * Specify a plugin under test, Formatter or ConcurrentFormatter
         *
         * @param formatter the plugin under test
         * @return this instance
         */
        public Builder withFormatterUnderTest(Object formatter) {
            this.instance.formatterUnderTest = formatter;
            return this;
        }

        public Builder withRuntimeArgs(String... runtimeArgs) {
            return withRuntimeArgs(Arrays.asList(runtimeArgs));
        }

        public Builder withRuntimeArgs(List<String> runtimeArgs) {
            this.instance.runtimeArgs = runtimeArgs;
            return this;
        }

        public TestHelper build() {
            return this.instance;
        }
    }

}
