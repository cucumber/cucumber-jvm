package cucumber.runtime;

import cucumber.api.Scenario;
import cucumber.runner.Runner;
import cucumber.runtime.io.ClasspathResourceLoader;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {
    private final static String ENGLISH = "en";

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     * TODO: ensure this is no longer needed with the alternate approach taken in Runtime
     * TODO: this test is rather brittle, since there's lots of mocking :(
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        Backend backend = mock(Backend.class);
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(PickleTag.class))).thenReturn(true);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, asList(backend), runtimeOptions);
        runtime.getGlue().addAfterHook(hook);
        Runner runner = runtime.getRunner();
        PickleEvent pickleEvent = new PickleEvent("uri", new Pickle("name", ENGLISH, Collections.<PickleStep>emptyList(), Collections.<PickleTag>emptyList(), asList(mock(PickleLocation.class))));

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(Matchers.<Scenario>any());
        inOrder.verify(backend).disposeWorld();
    }


}
