package cucumber.runtime;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SkipTagsTest {

    @Test
    public void noSkipTagsTest() throws Exception {
        String stats = runSkipTagsTest();

        assertThat(stats, startsWith(String.format(
                "5 Scenarios (5 passed)%n" +
                        "15 Steps (15 passed)%n")));
    }

    @Test
    public void singleSkipTagsTest() throws Exception {
        String stats = runSkipTagsTest("@Skip");

        assertThat(stats, startsWith(String.format(
                "5 Scenarios (1 skipped, 4 passed)%n" +
                        "15 Steps (3 skipped, 12 passed)%n")));
    }

    @Test
    public void multipleSkipTagsTest() throws Exception {
        String stats = runSkipTagsTest("@Skip", "@SkipAlso");

        assertThat(stats, startsWith(String.format(
                "5 Scenarios (2 skipped, 3 passed)%n" +
                        "15 Steps (6 skipped, 9 passed)%n")));
    }

    @Test
    public void negatedSkipTagsTest() throws Exception {
        String stats = runSkipTagsTest("~@NeverSkip");

        assertThat(stats, startsWith(String.format(
                "5 Scenarios (4 skipped, 1 passed)%n" +
                        "15 Steps (12 skipped, 3 passed)%n")));
    }

    @Test
    public void multipleNegatedSkipTagsTest() throws Exception {
        String stats = runSkipTagsTest("~@NeverSkip", "~@AlsoNeverSkip");

        assertThat(stats, startsWith(String.format(
                "5 Scenarios (2 skipped, 3 passed)%n" +
                        "15 Steps (6 skipped, 9 passed)%n")));
    }

    public String runSkipTagsTest(String...skipTags) throws Exception {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        RuntimeGlue runtimeGlue = new RuntimeGlue(mock(UndefinedStepsTracker.class), mock(LocalizedXStreams.class));
        runtimeGlue.addStepDefinition(new ArgumentMatchingStubStepDefinition("skip_test"));

        List<String> args = new ArrayList<String>();
        args.add("--monochrome");

        for (String skipTag : skipTags) {
            args.add("--skip-tags");
            args.add(skipTag);
        }

        args.add("cucumber/runtime/SkipTagsTest.feature");

        RuntimeOptions runtimeOptions = new RuntimeOptions(args);

        Backend backend = mock(Backend.class);
        when(backend.getSnippet(any(Step.class), any(FunctionNameGenerator.class))).thenReturn("TEST SNIPPET");
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, StopWatch.SYSTEM, runtimeGlue);
        runtime.run();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        runtime.printStats(new PrintStream(baos));

        return baos.toString();
    }


    private static class ArgumentMatchingStubStepDefinition extends StubStepDefinition {
        public ArgumentMatchingStubStepDefinition(String pattern) throws NoSuchMethodException {
            super(new Object(), Object.class.getMethod("toString"), pattern);
        }

        @Override
        public List<Argument> matchedArguments(Step step) {
            return Collections.EMPTY_LIST;
        }
    }
}
