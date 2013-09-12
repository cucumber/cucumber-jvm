package cucumber.runtime;

import cucumber.runtime.Env;
import cucumber.runtime.io.ClasspathResourceLoader;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import cucumber.runtime.formatter.StepMatcher;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import cucumber.api.PendingException;
import gherkin.I18n;
import junit.framework.AssertionFailedError;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Ignore;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@Ignore
public class TestHelper {
    public static CucumberFeature feature(final String path, final String source) throws IOException {
        ArrayList<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        FeatureBuilder featureBuilder = new FeatureBuilder(cucumberFeatures);
        featureBuilder.parse(new Resource() {
            @Override
            public String getPath() {
                return path;
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
            public String getClassName() {
                throw new UnsupportedOperationException();
            }
        }, new ArrayList<Object>());
        return cucumberFeatures.get(0);
    }

    public static void runFeatureWithFormatter(final CucumberFeature feature, final Map<String, String> stepsToResult, final List<SimpleEntry<String, String>> hooks,
            final long stepHookDuration, final Formatter formatter, final Reporter reporter) throws Throwable, FileNotFoundException {
        final RuntimeOptions runtimeOptions = new RuntimeOptions(new Env());
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final RuntimeGlue glue = createMockedRuntimeGlueThatMatchesTheSteps(stepsToResult, hooks);
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions, new StopWatch.Stub(stepHookDuration), glue);

        feature.run(formatter, reporter, runtime);
        formatter.done();
        formatter.close();
    }

    private static RuntimeGlue createMockedRuntimeGlueThatMatchesTheSteps(Map<String, String> stepsToResult,
            final List<SimpleEntry<String, String>> hooks) throws Throwable {
        RuntimeGlue glue = mock(RuntimeGlue.class);
        TestHelper.mockSteps(glue, stepsToResult);
        TestHelper.mockHooks(glue, hooks);
        return glue;
    }

    private static void mockSteps(RuntimeGlue glue, Map<String, String> stepsToResult) throws Throwable {
        for (String stepName : stepsToResult.keySet()) {
            if (!"undefined".equals(stepsToResult.get(stepName))) {
                StepDefinitionMatch matchStep = mock(StepDefinitionMatch.class);
                when(glue.stepDefinitionMatch(anyString(), TestHelper.stepWithName(stepName), (I18n) any())).thenReturn(matchStep);
                if ("pending".equals(stepsToResult.get(stepName))) {
                    doThrow(new PendingException()).when(matchStep).runStep((I18n) any());
                } else if ("failed".equals(stepsToResult.get(stepName))) {
                    AssertionFailedError error = TestHelper.mockAssertionFailedError();
                    doThrow(error).when(matchStep).runStep((I18n) any());
                } else if (!"passed".equals(stepsToResult.get(stepName)) &&
                        !"skipped".equals(stepsToResult.get(stepName))) {
                    fail("Cannot mock step to the result: " + stepsToResult.get(stepName));
                }
            }
        }
    }

    private static void mockHooks(RuntimeGlue glue, final List<SimpleEntry<String, String>> hooks) throws Throwable {
        List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
        List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
        for (SimpleEntry<String, String> hookEntry : hooks) {
            TestHelper.mockHook(hookEntry, beforeHooks, afterHooks);
        }
        if (beforeHooks.size() != 0) {
            when(glue.getBeforeHooks()).thenReturn(beforeHooks);
        }
        if (afterHooks.size() != 0) {
            when(glue.getAfterHooks()).thenReturn(afterHooks);
        }
    }

    private static void mockHook(SimpleEntry<String, String> hookEntry, List<HookDefinition> beforeHooks,
            List<HookDefinition> afterHooks) throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyCollectionOf(Tag.class))).thenReturn(true);
        if (hookEntry.getValue().equals("failed")) {
            AssertionFailedError error = TestHelper.mockAssertionFailedError();
            doThrow(error).when(hook).execute((cucumber.api.Scenario) any());
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

    public static SimpleEntry<String, String> hookEntry(String type, String result) {
        return new SimpleEntry<String, String>(type, result);
    }
}
