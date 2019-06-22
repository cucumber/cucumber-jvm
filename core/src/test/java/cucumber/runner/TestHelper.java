package cucumber.runner;

import cucumber.api.PendingException;
import cucumber.api.Plugin;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventListener;
import cucumber.runtime.BackendSupplier;
import io.cucumber.core.options.CommandlineOptionsParser;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Runtime;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StubStepDefinition;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureParser;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;
import io.cucumber.core.model.FeatureIdentifier;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.TypeRegistry;
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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cucumber.api.Result.Type.FAILED;
import static cucumber.api.Result.Type.PASSED;
import static cucumber.api.Result.Type.PENDING;
import static cucumber.api.Result.Type.SKIPPED;
import static cucumber.api.Result.Type.UNDEFINED;
import static java.util.Locale.ENGLISH;
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
    private Map<String, Result> stepsToResult = Collections.emptyMap();
    private Map<String, String> stepsToLocation = Collections.emptyMap();
    private List<SimpleEntry<String, Result>> hooks = Collections.emptyList();
    private List<String> hookLocations = Collections.emptyList();
    private List<Answer<Object>> hookActions = Collections.emptyList();
    private TimeServiceType timeServiceType = TimeServiceType.FIXED_INCREMENT_ON_STEP_START;
    private long timeServiceIncrement = 0L;
    private Object formatterUnderTest = null;
    private List<String> runtimeArgs = Collections.emptyList();

    private TestHelper() {
    }

    private static final class TestHelperBackendSupplier extends TestBackendSupplier {

        private final List<CucumberFeature> features;
        private final Map<String, Result> stepsToResult;
        private final Map<String, String> stepsToLocation;
        private final List<SimpleEntry<String, Result>> hooks;
        private final List<String> hookLocations;
        private final List<Answer<Object>> hookActions;

        private TestHelperBackendSupplier(List<CucumberFeature> features, Map<String, Result> stepsToResult, Map<String, String> stepsToLocation, List<SimpleEntry<String, Result>> hooks, List<String> hookLocations, List<Answer<Object>> hookActions) {
            this.features = features;
            this.stepsToResult = stepsToResult;
            this.stepsToLocation = stepsToLocation;
            this.hooks = hooks;
            this.hookLocations = hookLocations;
            this.hookActions = hookActions;
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
                                      Map<String, Result> stepsToResult,
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
                final Result stepResult = getResultWithDefaultPassed(stepsToResult, step.getText());
                if (stepResult.is(UNDEFINED)) {
                    continue;
                }

                Type[] types = mapArgumentToTypes(step);
                StepDefinition stepDefinition = new StubStepDefinition(step.getText(), typeRegistry, types) {

                    @Override
                    public void execute(Object[] args) throws Throwable {
                        super.execute(args);
                        if (stepResult.is(PENDING)) {
                            throw new PendingException();
                        } else if (stepResult.is(FAILED)) {
                            throw stepResult.getError();
                        } else if (stepResult.is(SKIPPED) && (stepResult.getError() != null)) {
                            throw stepResult.getError();
                        } else if (!stepResult.is(PASSED) && !stepResult.is(SKIPPED)) {
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


        private static Result getResultWithDefaultPassed(Map<String, Result> stepsToResult, String step) {
            return stepsToResult.containsKey(step) ? stepsToResult.get(step) : new Result(PASSED, 0L, null);
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

        private static void mockHooks(cucumber.runtime.Glue glue, final List<SimpleEntry<String, Result>> hooks,
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

        private static void mockHook(final SimpleEntry<String, Result> hookEntry,
                                     final String hookLocation,
                                     final Answer<Object> action,
                                     final List<HookDefinition> beforeHooks,
                                     final List<HookDefinition> afterHooks,
                                     final List<HookDefinition> beforeStepHooks,
                                     final List<HookDefinition> afterStepHooks) throws Throwable {
            HookDefinition hook = mock(HookDefinition.class);
            when(hook.matches(ArgumentMatchers.<PickleTag>anyCollection())).thenReturn(true);
            if (hookLocation != null) {
                when(hook.getLocation(anyBoolean())).thenReturn(hookLocation);
            }
            if (action != null) {
                doAnswer(action).when(hook).execute((Scenario) any());
            }
            if (hookEntry.getValue().is(FAILED)) {
                doThrow(hookEntry.getValue().getError()).when(hook).execute((cucumber.api.Scenario) any());
            } else if (hookEntry.getValue().is(PENDING)) {
                doThrow(new PendingException()).when(hook).execute((cucumber.api.Scenario) any());
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
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);


        final BackendSupplier backendSupplier = new TestHelperBackendSupplier(
            features,
            stepsToResult,
            stepsToLocation,
            hooks,
            hookLocations,
            hookActions
        );

        final FeatureSupplier featureSupplier = features.isEmpty()
            ? null // assume feature paths passed in as args instead
            : new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return features;
            }
        };

        Runtime.Builder runtimeBuilder = Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(runtimeArgs)
                    .build()
            )
            .withClassLoader(classLoader)
            .withResourceLoader(resourceLoader)
            .withBackendSupplier(backendSupplier)
            .withFeatureSupplier(featureSupplier);

        if (TimeServiceType.REAL_TIME.equals(this.timeServiceType)) {
            if (formatterUnderTest instanceof Plugin) {
                runtimeBuilder.withAdditionalPlugins((Plugin) formatterUnderTest);
            }
        } else {
            EventBus bus = null;
            if (TimeServiceType.FIXED_INCREMENT_ON_STEP_START.equals(this.timeServiceType)) {
                final StepDurationTimeService timeService = new StepDurationTimeService(this.timeServiceIncrement);
                bus = new TimeServiceEventBus(timeService);
                timeService.setEventPublisher(bus);
            } else if (TimeServiceType.FIXED_INCREMENT.equals(this.timeServiceType)) {
                bus = new TimeServiceEventBus(new TimeServiceStub(this.timeServiceIncrement));
            }

            runtimeBuilder.withEventBus(bus);
            if (formatterUnderTest instanceof ConcurrentEventListener) {
                ((ConcurrentEventListener) formatterUnderTest).setEventPublisher(bus);
            } else if (formatterUnderTest instanceof EventListener) {
                ((EventListener) formatterUnderTest).setEventPublisher(bus);
            }
        }

        runtimeBuilder.build().run();
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
        public Builder withTimeServiceIncrement(long timeServiceIncrement) {
            this.instance.timeServiceIncrement = timeServiceIncrement;
            return this;
        }

        /**
         * Specifies what type of TimeService to be used by the {@link EventBus}
         * {@link TimeServiceType#REAL_TIME} > {@link TimeService#SYSTEM}
         * {@link TimeServiceType#FIXED_INCREMENT} > {@link TimeServiceStub}
         * {@link TimeServiceType#FIXED_INCREMENT_ON_STEP_START} > {@link StepDurationTimeService}
         * <p>
         * Defaults to {@link TimeServiceType#FIXED_INCREMENT_ON_STEP_START}
         * <p>
         * Note: when running tests with multiple threads & not using {@link TimeServiceType#REAL_TIME}
         * it can inadvertently affect the order of {@link cucumber.api.event.Event}s
         * published to any {@link cucumber.api.event.ConcurrentEventListener}s used during the test run
         *
         * @return this instance
         */
        public Builder withTimeServiceType(TimeServiceType timeServiceType) {
            this.instance.timeServiceType = timeServiceType;
            return this;
        }

        /**
         * Specify a formatter under test, Formatter or ConcurrentFormatter
         *
         * @param formatter the formatter under test
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

    public static Result result(String status) {
        return result(Result.Type.fromLowerCaseName(status));
    }

    public static Result result(String status, Throwable error) {
        return result(Result.Type.fromLowerCaseName(status), error);
    }

    public static Result result(Result.Type status) {
        switch (status) {
            case FAILED:
                return result(status, mockAssertionFailedError());
            case AMBIGUOUS:
                return result(status, mockAmbiguousStepDefinitionException());
            case PENDING:
                return result(status, new PendingException());
            default:
                return result(status, null);
        }
    }

    public static Result result(Result.Type status, Throwable error) {
        return new Result(status, 0L, error);
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
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Scenario scenario = (Scenario) invocation.getArguments()[0];
                scenario.embed(data, mimeType);
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
        return exception;
    }

    public static SimpleEntry<String, Result> hookEntry(String type, Result result) {
        return new SimpleEntry<>(type, result);
    }

}
