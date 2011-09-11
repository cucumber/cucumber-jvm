package cucumber.runtime;

import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

public class HookTest {

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        Backend backend = mock(Backend.class);
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(String.class))).thenReturn(true);

        List<Backend> backendList = new ArrayList<Backend>();
        backendList.add(backend);
        World world = new World(backendList, mock(Runtime.class), new ArrayList<String>());
        world.addAfterHook(hook);

        world.dispose();

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(null);
        inOrder.verify(backend).disposeWorld();
    }

}
