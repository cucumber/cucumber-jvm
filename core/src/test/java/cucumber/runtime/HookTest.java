package cucumber.runtime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class HookTest {

    private static final List<String> TAGS = new ArrayList<String>();
    private static final List<String> CODE_PATHS = new ArrayList<String>();

    private Backend backend;
    private HookDefinition hook;
    private World world;

    @Before
    public void setUp() {
        backend = mock(Backend.class);
        hook = mock(HookDefinition.class);

        Runtime runtime = new Runtime(CODE_PATHS, asList(backend), false);
        world = new World(runtime, TAGS);
        world.addAfterHook(hook);
    }

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        when(hook.matches(anyListOf(String.class))).thenReturn(true);

        world.dispose();

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(null);
        inOrder.verify(backend).disposeWorld();
    }

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/89">#89</a>.
     */
    @Test
    public void objects_are_disposed_even_when_after_hooks_throw_up() throws Throwable {
        when(hook.matches(anyListOf(String.class))).thenReturn(true);
        doThrow(new RuntimeException("test exception")).when(hook).execute(null);

        try {
            world.dispose();
        } catch(CucumberException e) {
            // expected
        }

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(null);
        inOrder.verify(backend).disposeWorld();
    }

}
