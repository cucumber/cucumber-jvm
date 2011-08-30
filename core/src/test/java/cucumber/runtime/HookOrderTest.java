package cucumber.runtime;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class HookOrderTest {

    private World world;
    private Backend[] backends;    
        
    @Before
    public void buildMockWorld() {       
        backends = new Backend[] {mock(Backend.class), mock(Backend.class)};
        world = new World(Arrays.asList(backends), mock(Runtime.class), new ArrayList<String>());
    }
    
    @Test
    public void before_hooks_execute_in_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        when(backends[0].getBeforeHooks()).thenReturn(hooks);      
        
        world.prepare();
        
        InOrder inOrder = inOrder(hooks.toArray());        
        inOrder.verify(hooks.get(2)).execute();
        inOrder.verify(hooks.get(0)).execute();
        inOrder.verify(hooks.get(1)).execute();
    }
    
    @Test
    public void after_hooks_execute_in_reverse_order() throws Throwable {
        List<HookDefinition> hooks = mockHooks(2, Integer.MAX_VALUE, 4);
        when(backends[0].getAfterHooks()).thenReturn(hooks); 

        world.dispose();
        
        InOrder inOrder = inOrder(hooks.toArray());        
        inOrder.verify(hooks.get(1)).execute();        
        inOrder.verify(hooks.get(2)).execute();
        inOrder.verify(hooks.get(0)).execute();
    }
    
    @Test
    public void hooks_order_across_many_backends() throws Throwable {
        List<HookDefinition> backend1Hooks = mockHooks(3, Integer.MAX_VALUE, 1);
        List<HookDefinition> backend2Hooks = mockHooks(2, Integer.MAX_VALUE, 4);
        when(backends[0].getBeforeHooks()).thenReturn(backend1Hooks);        
        when(backends[1].getBeforeHooks()).thenReturn(backend2Hooks); 

        world.prepare();
        
        List<HookDefinition> allHooks = new ArrayList<HookDefinition>();
        allHooks.addAll(backend1Hooks);
        allHooks.addAll(backend2Hooks);
        
        InOrder inOrder = inOrder(allHooks.toArray());        
        inOrder.verify(backend1Hooks.get(2)).execute();        
        inOrder.verify(backend2Hooks.get(0)).execute();
        inOrder.verify(backend1Hooks.get(0)).execute();
        inOrder.verify(backend2Hooks.get(2)).execute();
        verify(backend2Hooks.get(1)).execute();
        verify(backend1Hooks.get(1)).execute();
    }
    
    private List<HookDefinition> mockHooks(int ... ordering) {
        List<HookDefinition> hooks = new ArrayList<HookDefinition>();
        for(int order:ordering) {
            HookDefinition hook = mock(HookDefinition.class);
            when(hook.getOrder()).thenReturn(order);
            when(hook.matches(anyListOf(String.class))).thenReturn(true);
            hooks.add(hook);
        }
        return hooks;
    }
}
