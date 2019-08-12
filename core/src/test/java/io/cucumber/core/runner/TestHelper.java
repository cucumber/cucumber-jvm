package io.cucumber.core.runner;

import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.plugin.Plugin;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.plugin.ConcurrentEventListener;
import io.cucumber.core.plugin.EventListener;
import io.cucumber.core.event.Event;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.*;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.TestClasspathResourceLoader;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.feature.CucumberFeature;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.datatable.DataTable;
import io.cucumber.core.stepexpression.TypeRegistry;
import junit.framework.AssertionFailedError;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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

import static io.cucumber.core.event.Status.FAILED;
import static io.cucumber.core.event.Status.PASSED;
import static io.cucumber.core.event.Status.PENDING;
import static io.cucumber.core.event.Status.SKIPPED;
import static io.cucumber.core.event.Status.UNDEFINED;
import static java.time.Duration.ZERO;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.ROOT;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestHelper {

    public enum TimeServiceType {
        REAL_TIME, FIXED_INCREMENT, FIXED_INCREMENT_ON_STEP_START
    }

    private List<CucumberFeature> features = Collections.emptyList();
    private Map<String, io.cucumber.core.event.Result> stepsToResult = Collections.emptyMap();
    private Map<String, String> stepsToLocation = Collections.emptyMap();
    private List<SimpleEntry<String, io.cucumber.core.event.Result>> hooks = Collections.emptyList();
    private List<String> hookLocations = Collections.emptyList();
    private List<Answer<Object>> hookActions = Collections.emptyList();
    private TimeServiceType timeServiceType = TimeServiceType.FIXED_INCREMENT_ON_STEP_START;
    private Duration timeServiceIncrement = Duration.ZERO;
    private Object formatterUnderTest = null;
    private List<String> runtimeArgs = Collections.emptyList();

    private TestHelper() {
    }

    public static final class TestHelperBackendSupplier extends TestBackendSupplier {

        private final List<CucumberFeature> features;
        private final Map<String, io.cucumber.core.event.Result> stepsToResult;
        private final Map<String, String> stepsToLocation;
        private final List<SimpleEntry<String, io.cucumber.core.event.Result>> hooks;
        private final List<String> hookLocations;
        private final List<Answer<Object>> hookActions;

        public TestHelperBackendSupplier(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
            this(
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
            );
        }

        public TestHelperBackendSupplier(List<CucumberFeature> features, Map<String, io.cucumber.core.event.Result> stepsToResult, Map<String, String> stepsToLocation, List<SimpleEntry<String, io.cucumber.core.event.Result>> hooks, List<String> hookLocations, List<Answer<Object>> hookActions) {
            this.features = features;
            this.stepsToResult = stepsToResult;
            this.stepsToLocation = stepsToLocation;
            this.hooks = hooks;
            this.hookLocations = hookLocations;
            this.hookActions = hookActions;
        }

        public TestHelperBackendSupplier(List<CucumberFeature> features) {
            this(
                features,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
            );
        }


        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
            try {
                mockSteps(glue, features, stepsToResult, stepsToLocation);
                mockHooks(glue, hooks, hookLocations, hookActions);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        private static void mockSteps(Glue glue, List<CucumberFeature> features,
                                      Map<String, io.cucumber.core.event.Result> stepsToResult,
                                      final Map<String, String> stepsToLocation) {
            Compiler compiler = new Compiler();
            TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);

            List<PickleStep> steps = new ArrayList<>();
            for (CucumberFeature feature : features) {
                for (Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
                    for (PickleStep step : pickle.getSteps()) {
                        if (!containsStep(steps, step)) {
                            steps.add(step);
                        }
                    }
                }
            }

            for (final PickleStep step : steps) {
                final io.cucumber.core.event.Result stepResult = getResultWithDefaultPassed(stepsToResult, step.getText());
                if (stepResult.getStatus().is(UNDEFINED)) {
                    continue;
                }

                Type[] types = mapArgumentToTypes(step);
                StepDefinition stepDefinition = new StubStepDefinition(step.getText(), types) {

                    @Override
                    public void execute(Object[] args) throws Throwable {
                        super.execute(args);
                        if (stepResult.getStatus().is(PENDING)) {
                            throw new TestPendingException();
                        } else if (stepResult.getStatus().is(FAILED)) {
                            throw stepResult.getError();
                        } else if (stepResult.getStatus().is(SKIPPED) && (stepResult.getError() != null)) {
                            throw stepResult.getError();
                        } else if (!stepResult.getStatus().is(PASSED) && !stepResult.getStatus().is(SKIPPED)) {
                            fail("Cannot mock step to the result: " + stepResult.getStatus());
                        }
                    }

                    @Override
                    public String getLocation(boolean detail) {
                        return stepsToLocation.get(step.getText());
                    }
                };

                glue.addStepDefinition(stepDefinition);
            }
        }


        private static io.cucumber.core.event.Result getResultWithDefaultPassed(Map<String, io.cucumber.core.event.Result> stepsToResult, String step) {
            return stepsToResult.containsKey(step) ? stepsToResult.get(step) : new Result(PASSED, ZERO, null);
        }


        private static boolean containsStep(List<PickleStep> steps, PickleStep step) {
            for (PickleStep definedSteps : steps) {
                if (definedSteps.getText().equals(step.getText())
                    && definedSteps.getArgument().size() == step.getArgument().size()
                ) {
                    return true;
                }
            }

            return false;
        }

        private static Type[] mapArgumentToTypes(PickleStep step) {
            Type[] types = new Type[0];
            if (step.getArgument().isEmpty()) {
                return types;
            } else if (step.getArgument().get(0) instanceof PickleString) {
                types = new Type[]{String.class};
            } else if (step.getArgument().get(0) instanceof PickleTable) {
                types = new Type[]{DataTable.class};
            }
            return types;
        }

        private static void mockHooks(Glue glue, final List<SimpleEntry<String, io.cucumber.core.event.Result>> hooks,
                                      final List<String> hookLocations,
                                      final List<Answer<Object>> hookActions) throws Throwable {
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

        private static void mockHook(final SimpleEntry<String, io.cucumber.core.event.Result> hookEntry,
                                     final String hookLocation,
                                     final Answer<Object> action,
                                     final List<HookDefinition> beforeHooks,
                                     final List<HookDefinition> afterHooks,
                                     final List<HookDefinition> beforeStepHooks,
                                     final List<HookDefinition> afterStepHooks) throws Throwable {
            HookDefinition hook = mock(HookDefinition.class);
            when(hook.getTagExpression()).thenReturn("");
            if (hookLocation != null) {
                when(hook.getLocation(anyBoolean())).thenReturn(hookLocation);
            }
            if (action != null) {
                doAnswer(action).when(hook).execute((Scenario) any());
            }
            if (hookEntry.getValue().getStatus().is(FAILED)) {
                doThrow(hookEntry.getValue().getError()).when(hook).execute((Scenario) any());
            } else if (hookEntry.getValue().getStatus().is(PENDING)) {
                doThrow(new TestPendingException()).when(hook).execute((io.cucumber.core.api.Scenario) any());
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

    }

    public void run() {

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ResourceLoader resourceLoader = TestClasspathResourceLoader.create(classLoader);


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
            .withResourceLoader(resourceLoader)
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
            bus = new TimeServiceEventBus(Clock.systemUTC());
        } else if (TimeServiceType.FIXED_INCREMENT_ON_STEP_START.equals(this.timeServiceType)) {
            final StepDurationTimeService timeService = new StepDurationTimeService(this.timeServiceIncrement);
            bus = new TimeServiceEventBus(timeService);
            timeService.setEventPublisher(bus);
        } else if (TimeServiceType.FIXED_INCREMENT.equals(this.timeServiceType)) {
            bus = new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")));
        }
        return bus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final TestHelper instance = new TestHelper();

        private Builder() {
        }

        public Builder withFeatures(CucumberFeature... features) {
            return withFeatures(Arrays.asList(features));
        }

        public Builder withFeatures(List<CucumberFeature> features) {
            this.instance.features = features;
            return this;
        }

        public Builder withStepsToResult(Map<String, io.cucumber.core.event.Result> stepsToResult) {
            this.instance.stepsToResult = stepsToResult;
            return this;
        }

        public Builder withStepsToLocation(Map<String, String> stepsToLocation) {
            this.instance.stepsToLocation = stepsToLocation;
            return this;
        }

        public Builder withHooks(List<SimpleEntry<String, io.cucumber.core.event.Result>> hooks) {
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

    public static CucumberFeature feature(final String uri, final String source) {
        return feature(FeatureIdentifier.parse(uri), source);
    }

    public static CucumberFeature feature(final URI uri, final String source) {
        return FeatureParser.parseResource(new Resource() {
            @Override
            public URI getPath() {
                return uri;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }

        });
    }

    public static io.cucumber.core.event.Result result(String status) {
        return result(fromLowerCaseName(status));
    }

    public static io.cucumber.core.event.Result result(String status, Throwable error) {
        return result(fromLowerCaseName(status), error);
    }

    private static Status fromLowerCaseName(String lowerCaseName) {
        return Status.valueOf(lowerCaseName.toUpperCase(ROOT));
    }

    public static io.cucumber.core.event.Result result(Status status) {
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

    public static io.cucumber.core.event.Result result(Status status, Throwable error) {
        return new Result(status, Duration.ZERO, error);
    }

    public static Answer<Object> createWriteHookAction(final String output) {
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Scenario scenario = (Scenario) invocation.getArguments()[0];
                scenario.write(output);
                return null;
            }
        };
    }

    public static Answer<Object> createEmbedHookAction(final byte[] data, final String mimeType) {
        return createEmbedHookAction(data, mimeType, null);
    }

    public static Answer<Object> createEmbedHookAction(final byte[] data, final String mimeType, final String name) {
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Scenario scenario = (Scenario) invocation.getArguments()[0];
                if (name != null) {
                    scenario.embed(data, mimeType, name);
                } else {
                    scenario.embed(data, mimeType);
                }
                return null;
            }
        };
    }

    private static AssertionFailedError mockAssertionFailedError() {
        AssertionFailedError error = mock(AssertionFailedError.class);
        Answer<Object> printStackTraceHandler = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrintWriter writer = (PrintWriter) invocation.getArguments()[0];
                writer.print("the stack trace");
                return null;
            }
        };
        doAnswer(printStackTraceHandler).when(error).printStackTrace((PrintWriter) any());
        when(error.getStackTrace()).thenReturn(new StackTraceElement[0]);
        when(error.getMessage()).thenReturn("the message");
        return error;
    }

    private static AmbiguousStepDefinitionsException mockAmbiguousStepDefinitionException() {
        AmbiguousStepDefinitionsException exception = mock(AmbiguousStepDefinitionsException.class);
        Answer<Object> printStackTraceHandler = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrintWriter writer = (PrintWriter) invocation.getArguments()[0];
                writer.print("the stack trace");
                return null;
            }
        };
        doAnswer(printStackTraceHandler).when(exception).printStackTrace((PrintWriter) any());
        when(exception.getMessage()).thenReturn("the message");
        return exception;
    }

    public static SimpleEntry<String, io.cucumber.core.event.Result> hookEntry(String type, io.cucumber.core.event.Result result) {
        return new SimpleEntry<>(type, result);
    }

}
