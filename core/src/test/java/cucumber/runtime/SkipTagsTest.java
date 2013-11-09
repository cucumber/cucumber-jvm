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
    public void skipTagsTest() throws Exception {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        RuntimeGlue runtimeGlue = new RuntimeGlue(mock(UndefinedStepsTracker.class), mock(LocalizedXStreams.class));
        runtimeGlue.addStepDefinition(new ArgumentMatchingStubStepDefinition("skip_test"));

        List<String> args = new ArrayList<String>();
        args.add("--monochrome");
        args.add("--skip-tags");
        args.add("@Skip");
        args.add("cucumber/runtime/SkipTagsTest.feature");

        RuntimeOptions runtimeOptions = new RuntimeOptions(args);

        Backend backend = mock(Backend.class);
        when(backend.getSnippet(any(Step.class), any(FunctionNameGenerator.class))).thenReturn("TEST SNIPPET");
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, StopWatch.SYSTEM, runtimeGlue);
        runtime.run();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "4 Scenarios (2 skipped, 2 passed)%n" +
                        "12 Steps (6 skipped, 6 passed)%n")));
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
