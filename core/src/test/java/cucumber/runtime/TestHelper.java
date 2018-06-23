package cucumber.runtime;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.formatter.Formatter;
import cucumber.runner.EventBus;
import cucumber.runner.StepDurationTimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.formatter.PickleStepMatcher;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import junit.framework.AssertionFailedError;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollectionOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestHelper {
    private TestHelper() {
    }

    public static CucumberFeature feature(final String path, final String source) {
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();

        GherkinDocument gherkinDocument = parser.parse(source, matcher);
        return new CucumberFeature(gherkinDocument, path, source);
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
        Answer<Object> writer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Scenario scenario = (Scenario) invocation.getArguments()[0];
                scenario.write(output);
                return null;
            }
        };
        return writer;
    }

    public static Answer<Object> createEmbedHookAction(final byte[] data, final String mimeType) {
        Answer<Object> embedder = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Scenario scenario = (Scenario) invocation.getArguments()[0];
                scenario.embed(data, mimeType);
                return null;
            }
        };
        return embedder;
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final List<SimpleEntry<String, Result>> hooks,
            final long stepHookDuration, final Formatter formatter) throws Throwable {
        runFeaturesWithFormatter(Arrays.asList(feature), stepsToResult, Collections.<String,String>emptyMap(), hooks, Collections.<String>emptyList(), Collections.<Answer<Object>>emptyList(), stepHookDuration, formatter);
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final long stepHookDuration, final Formatter formatter) throws Throwable {
        runFeaturesWithFormatter(Arrays.asList(feature), stepsToResult, stepsToLocation, hooks, Collections.<String>emptyList(), Collections.<Answer<Object>>emptyList(), stepHookDuration, formatter);
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations, final long stepHookDuration, final Formatter formatter) throws Throwable {
        runFeaturesWithFormatter(Arrays.asList(feature), stepsToResult, stepsToLocation, hooks, hookLocations, Collections.<Answer<Object>>emptyList(), stepHookDuration, formatter);
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
            final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations, final List<Answer<Object>> hookActions, final long stepHookDuration, final Formatter formatter) throws Throwable {
        runFeaturesWithFormatter(Arrays.asList(feature), stepsToResult, stepsToLocation, hooks, hookLocations, hookActions, stepHookDuration, formatter);
    }

    public static void runFeaturesWithFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult,
                                                final List<SimpleEntry<String, Result>> hooks, final long stepHookDuration, final Formatter formatter) throws Throwable {
        runFeaturesWithFormatter(features, stepsToResult, Collections.<String,String>emptyMap(), hooks,
            Collections.<String>emptyList(), Collections.<Answer<Object>>emptyList(), stepHookDuration, formatter);
    }

    public static void runFeaturesWithFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult,
                                                final List<SimpleEntry<String, Result>> hooks, final long stepHookDuration, final Formatter formatter, int threads) throws Throwable {
        runFeaturesWithFormatter(features, stepsToResult, Collections.<String,String>emptyMap(), hooks,
            Collections.<String>emptyList(), Collections.<Answer<Object>>emptyList(),
            stepHookDuration, formatter, "--threads" , String.valueOf(threads));
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, String> stepsToLocation,
                                               final Formatter formatter) throws Throwable {
        runFeaturesWithFormatter(Arrays.asList(feature), Collections.<String, Result>emptyMap(), stepsToLocation,
            Collections.<SimpleEntry<String, Result>>emptyList(), Collections.<String>emptyList(), Collections.<Answer<Object>>emptyList(), 0L, formatter);
    }

    public static void runFeaturesWithFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult,
                                                final Map<String, String> stepsToLocation, final List<SimpleEntry<String, Result>> hooks,
                                                final List<String> hookLocations, final List<Answer<Object>> hookActions,
                                                final long stepHookDuration, final Formatter formatter, final String... runtimeArgs
    ) throws Throwable {
        runFeaturesWithFormatter(features, stepsToResult, stepsToLocation, hooks, hookLocations, hookActions, stepHookDuration, formatter, true, runtimeArgs);
    }

    public static void runFeaturesWithFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult,
                                                final Map<String, String> stepsToLocation, final List<SimpleEntry<String, Result>> hooks,
                                                final List<String> hookLocations, final List<Answer<Object>> hookActions,
                                                final long stepHookDuration, final Formatter formatter, final boolean useFixedIncrementTimeService, final String... runtimeArgs
    ) throws Throwable {

        final StringBuilder additionalArgs = new StringBuilder();
        for(final String arg : runtimeArgs) {
            additionalArgs.append(" ").append(arg);
        }

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

        final RuntimeGlue glue = createMockedRuntimeGlueThatMatchesTheSteps(stepsToResult, stepsToLocation, hooks, hookLocations, hookActions);
        final GlueSupplier glueSupplier = new GlueSupplier() {
            @Override
            public Glue get() {
                return glue;
            }
        };
        final BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(mock(Backend.class));
            }
        };
        final FeatureSupplier featureSupplier = new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return features;
            }
        };

        Runtime.Builder builder = Runtime.builder()
            .withArg("-p null" + additionalArgs.toString())
            .withClassLoader(classLoader)
            .withResourceLoader(resourceLoader)
            .withGlueSupplier(glueSupplier)
            .withBackendSupplier(backendSupplier)
            .withFeatureSupplier(featureSupplier);

        if (useFixedIncrementTimeService) {
            final StepDurationTimeService timeService = new StepDurationTimeService(stepHookDuration);
            final EventBus bus = new TimeServiceEventBus(timeService);
            if (formatter != null) {
                formatter.setEventPublisher(bus);
            }

            timeService.setEventPublisher(bus);
            builder.withEventBus(bus);
        } else {
            builder.withAdditionalPlugins(formatter);
        }

        builder.build().run();
    }

    private static RuntimeGlue createMockedRuntimeGlueThatMatchesTheSteps(final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
                                                                          final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations,
                                                                          final List<Answer<Object>> hookActions) throws Throwable {
        RuntimeGlue glue = mock(RuntimeGlue.class);
        TestHelper.mockSteps(glue, stepsToResult, stepsToLocation);
        TestHelper.mockHooks(glue, hooks, hookLocations, hookActions);
        return glue;
    }

    private static void mockSteps(RuntimeGlue glue, Map<String, Result> stepsToResult, Map<String, String> stepsToLocation) throws Throwable {
        for (String stepText : mergeStepSets(stepsToResult, stepsToLocation)) {
            Result stepResult = getResultWithDefaultPassed(stepsToResult, stepText);
            if (!stepResult.is(Result.Type.UNDEFINED)) {
                PickleStepDefinitionMatch matchStep = mock(PickleStepDefinitionMatch.class);
                when(glue.stepDefinitionMatch(anyString(), TestHelper.stepWithName(stepText))).thenReturn(matchStep);
                mockStepResult(stepResult, matchStep);
                mockStepLocation(getLocationWithDefaultEmptyString(stepsToLocation, stepText), matchStep);
            }
        }
    }

    private static void mockStepResult(Result stepResult, PickleStepDefinitionMatch matchStep) throws Throwable {
        if (stepResult.is(Result.Type.PENDING)) {
            doThrow(new PendingException()).when(matchStep).runStep(anyString(), (Scenario) any());
        } else if (stepResult.is(Result.Type.FAILED)) {
            doThrow(stepResult.getError()).when(matchStep).runStep(anyString(), (Scenario) any());
        } else if (stepResult.is(Result.Type.SKIPPED) && stepResult.getError() != null) {
            doThrow(stepResult.getError()).when(matchStep).runStep(anyString(), (Scenario) any());
        } else if (!stepResult.is(Result.Type.PASSED) &&
                   !stepResult.is(Result.Type.SKIPPED)) {
            fail("Cannot mock step to the result: " + stepResult.getStatus());
        }
    }

    private static void mockStepLocation(String stepLocation, PickleStepDefinitionMatch matchStep) {
        when(matchStep.getCodeLocation()).thenReturn(stepLocation);
    }

    private static void mockHooks(RuntimeGlue glue, final List<SimpleEntry<String, Result>> hooks, final List<String> hookLocations,
            final List<Answer<Object>> hookActions) throws Throwable {
        List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
        List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
        List<HookDefinition> beforeStepHooks = new ArrayList<HookDefinition>();
        List<HookDefinition> afterStepHooks = new ArrayList<HookDefinition>();
        for (int i = 0; i < hooks.size(); ++i) {
            String hookLocation = hookLocations.size() > i ? hookLocations.get(i) : null;
            Answer<Object> hookAction  = hookActions.size() > i ? hookActions.get(i) : null;
            TestHelper.mockHook(hooks.get(i), hookLocation, hookAction, beforeHooks, afterHooks, beforeStepHooks, afterStepHooks);
        }
        if (!beforeHooks.isEmpty()) {
            when(glue.getBeforeHooks()).thenReturn(beforeHooks);
        }
        if (!afterHooks.isEmpty()) {
            when(glue.getAfterHooks()).thenReturn(afterHooks);
        }
        if (!beforeStepHooks.isEmpty()) {
            when(glue.getBeforeStepHooks()).thenReturn(beforeStepHooks);
        }
        if (!afterStepHooks.isEmpty()) {
            when(glue.getAfterStepHooks()).thenReturn(afterStepHooks);
        }
    }

    private static void mockHook(final SimpleEntry<String, Result> hookEntry, final String hookLocation, final Answer<Object> action,
                                 final List<HookDefinition> beforeHooks, final List<HookDefinition> afterHooks, final List<HookDefinition> beforeStepHooks, final List<HookDefinition> afterStepHooks) throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyCollectionOf(PickleTag.class))).thenReturn(true);
        if (hookLocation != null) {
            when(hook.getLocation(anyBoolean())).thenReturn(hookLocation);
        }
        if (action != null) {
            doAnswer(action).when(hook).execute((Scenario)any());
        }
        if (hookEntry.getValue().is(Result.Type.FAILED)) {
            doThrow(hookEntry.getValue().getError()).when(hook).execute((cucumber.api.Scenario) any());
        } else if (hookEntry.getValue().is(Result.Type.PENDING)) {
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

    private static PickleStep stepWithName(String name) {
        return argThat(new PickleStepMatcher(name));
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
        return new SimpleEntry<String, Result>(type, result);
    }

    private static Set<String> mergeStepSets(Map<String, Result> stepsToResult, Map<String, String> stepsToLocation) {
        Set<String> steps = new HashSet<String>(stepsToResult.keySet());
        steps.addAll(stepsToLocation.keySet());
        return steps;
    }

    private static Result getResultWithDefaultPassed(Map<String, Result> stepsToResult, String step) {
        return stepsToResult.containsKey(step) ? stepsToResult.get(step) : new Result(Result.Type.PASSED, 0L, null);
    }

    private static String getLocationWithDefaultEmptyString(Map<String, String> stepsToLocation, String step) {
        return stepsToLocation.containsKey(step) ? stepsToLocation.get(step) : "";
    }

}
