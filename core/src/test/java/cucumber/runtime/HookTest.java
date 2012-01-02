package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import gherkin.formatter.Reporter;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {

    private static final List<String> TAGS = new ArrayList<String>();
    private static final List<String> CODE_PATHS = new ArrayList<String>();

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        Backend backend = mock(Backend.class);
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(String.class))).thenReturn(true);

        Runtime runtime = new Runtime(CODE_PATHS, new ClasspathResourceLoader(), asList(backend), false);
        World world = new RuntimeWorld(runtime, TAGS);
        world.addAfterHook(hook);

        world.runAfterHooksAndDisposeBackendWorlds(mock(Reporter.class));

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(backend).disposeWorld();
    }

}
