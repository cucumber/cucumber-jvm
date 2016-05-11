package cucumber.runtime;

import cucumber.api.PendingException;
import cucumber.runtime.formatter.StepMatcher;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import junit.framework.AssertionFailedError;
import org.junit.Ignore;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@Ignore
public class TestHelper {
    private TestHelper() {
    }

    public static CucumberFeature feature(final String path, final String source) throws IOException {
        ArrayList<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        FeatureBuilder featureBuilder = new FeatureBuilder(cucumberFeatures);
        featureBuilder.parse(new Resource() {
            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getAbsolutePath() {
                throw new UnsupportedOperationException();
            }

            @Override
            public InputStream getInputStream() {
                try {
                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getClassName(String extension) {
                throw new UnsupportedOperationException();
            }
        }, new ArrayList<Object>());
        return cucumberFeatures.get(0);
    }

    public static Result result(String status) {
        if (status.equals(Result.FAILED)) {
            return result(status, mockAssertionFailedError());
        } else if (status.equals("pending")){
            return result(status, new PendingException());
        } else {
            return new Result(status, 0L, null);
        }
    }

    public static Result result(String status, Throwable error) {
        return new Result(status, 0L, error, null);
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, Result> stepsToResult, final List<SimpleEntry<String, Result>> hooks,
            final long stepHookDuration, final Formatter formatter, final Reporter reporter) throws Throwable, FileNotFoundException {
        runFeaturesWithFormatter(Arrays.asList(feature), stepsToResult, Collections.<String,String>emptyMap(), hooks, stepHookDuration, formatter, reporter);
    }

    public static void runFeaturesWithFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult,
            final List<SimpleEntry<String, Result>> hooks, final long stepHookDuration, final Formatter formatter, final Reporter reporter) throws Throwable {
        runFeaturesWithFormatter(features, stepsToResult, Collections.<String,String>emptyMap(), hooks, stepHookDuration, formatter, reporter);
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, String> stepsToLocation,
                                               final Formatter formatter, final Reporter reporter) throws Throwable {        runFeaturesWithFormatter(Arrays.asList(feature), Collections.<String, Result>emptyMap(), stepsToLocation,
                Collections.<SimpleEntry<String, Result>>emptyList(), 0L, formatter, reporter);
    }

    private static void runFeaturesWithFormatter(final List<CucumberFeature> features, final Map<String, Result> stepsToResult, final Map<String, String> stepsToLocation,
                                                  final List<SimpleEntry<String, Result>> hooks, final long stepHookDuration, final Formatter formatter,
                                                  final Reporter reporter) throws Throwable {
        final RuntimeOptions runtimeOptions = new RuntimeOptions("");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final RuntimeGlue glue = createMockedRuntimeGlueThatMatchesTheSteps(stepsToResult, stepsToLocation, hooks);
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions, new StopWatch.Stub(stepHookDuration), glue);

        for (CucumberFeature feature : features) {
            feature.run(formatter, reporter, runtime);
        }
        formatter.done();
        formatter.close();
    }

    private static RuntimeGlue createMockedRuntimeGlueThatMatchesTheSteps(Map<String, Result> stepsToResult, Map<String, String> stepsToLocation,
                                                                          final List<SimpleEntry<String, Result>> hooks) throws Throwable {
        RuntimeGlue glue = mock(RuntimeGlue.class);
        TestHelper.mockSteps(glue, stepsToResult, stepsToLocation);
        TestHelper.mockHooks(glue, hooks);
        return glue;
    }

    private static void mockSteps(RuntimeGlue glue, Map<String, Result> stepsToResult, Map<String, String> stepsToLocation) throws Throwable {
        for (String stepName : mergeStepSets(stepsToResult, stepsToLocation)) {
            Result stepResult = getResultWithDefaultPassed(stepsToResult, stepName);
            if (!"undefined".equals(stepResult.getStatus())) {
                StepDefinitionMatch matchStep = mock(StepDefinitionMatch.class);
                when(glue.stepDefinitionMatch(anyString(), TestHelper.stepWithName(stepName), (I18n) any())).thenReturn(matchStep);
                mockStepResult(stepResult, matchStep);
                mockStepLocation(getLocationWithDefaultEmptyString(stepsToLocation, stepName), matchStep);
            }
        }
    }

    private static void mockStepResult(Result stepResult, StepDefinitionMatch matchStep) throws Throwable {
        if ("pending".equals(stepResult.getStatus())) {
            doThrow(new PendingException()).when(matchStep).runStep((I18n) any());
        } else if (Result.FAILED.equals(stepResult.getStatus())) {
            doThrow(stepResult.getError()).when(matchStep).runStep((I18n) any());
        } else if (!Result.PASSED.equals(stepResult.getStatus()) &&
                   !"skipped".equals(stepResult.getStatus())) {
            fail("Cannot mock step to the result: " + stepResult.getStatus());
        }
    }

    private static void mockStepLocation(String stepLocation, StepDefinitionMatch matchStep) {
        when(matchStep.getLocation()).thenReturn(stepLocation);
    }

    private static void mockHooks(RuntimeGlue glue, final List<SimpleEntry<String, Result>> hooks) throws Throwable {
        List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
        List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
        for (SimpleEntry<String, Result> hookEntry : hooks) {
            TestHelper.mockHook(hookEntry, beforeHooks, afterHooks);
        }
        if (!beforeHooks.isEmpty()) {
            when(glue.getBeforeHooks()).thenReturn(beforeHooks);
        }
        if (!afterHooks.isEmpty()) {
            when(glue.getAfterHooks()).thenReturn(afterHooks);
        }
    }

    private static void mockHook(SimpleEntry<String, Result> hookEntry, List<HookDefinition> beforeHooks,
                                 List<HookDefinition> afterHooks) throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyCollectionOf(Tag.class))).thenReturn(true);
        if (hookEntry.getValue().getStatus().equals("failed")) {
            doThrow(hookEntry.getValue().getError()).when(hook).execute((cucumber.api.Scenario) any());
        } else if (hookEntry.getValue().getStatus().equals("pending")) {
            doThrow(new PendingException()).when(hook).execute((cucumber.api.Scenario) any());
        }
        if ("before".equals(hookEntry.getKey())) {
            beforeHooks.add(hook);
        } else if ("after".equals(hookEntry.getKey())) {
            afterHooks.add(hook);
        } else {
            fail("Only before and after hooks are allowed, hook type found was: " + hookEntry.getKey());
        }
    }

    private static Step stepWithName(String name) {
        return argThat(new StepMatcher(name));
    }

    private static AssertionFailedError mockAssertionFailedError() {
        AssertionFailedError error = mock(AssertionFailedError.class);
        Answer<Object> printStackTraceHandler = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                PrintWriter writer = (PrintWriter) invocation.getArguments()[0];
                writer.print("the stack trace");
                return null;
            }
        };
        doAnswer(printStackTraceHandler).when(error).printStackTrace((PrintWriter) any());
        return error;
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
        return stepsToResult.containsKey(step) ? stepsToResult.get(step) : new Result(Result.PASSED, 0L, null);
    }

    private static String getLocationWithDefaultEmptyString(Map<String, String> stepsToLocation, String step) {
        return stepsToLocation.containsKey(step) ? stepsToLocation.get(step) : "";
    }
}
