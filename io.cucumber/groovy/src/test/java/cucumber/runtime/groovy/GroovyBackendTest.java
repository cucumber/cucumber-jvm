package cucumber.runtime.groovy;

import cucumber.runtime.io.ResourceLoader;
import org.codehaus.groovy.runtime.MethodClosure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class GroovyBackendTest {
    @Mock
    ResourceLoader resourceLoader;

    GroovyBackend backend;

    @Before
    public void setUp() throws Exception {
        backend = new GroovyBackend(resourceLoader);
    }

    @Test
    public void should_build_world_by_calling_the_closure() {
        backend.registerWorld(new MethodClosure(this, "worldClosureCall"));
        backend.buildWorld();

        GroovyWorld groovyWorld = backend.getGroovyWorld();
        assertEquals(1, groovyWorld.worldsCount());
    }

    @Test
    public void should_build_world_object_even_if_closure_world_was_not_added() {
        assertNull(backend.getGroovyWorld());

        backend.buildWorld();

        assertEquals(0, backend.getGroovyWorld().worldsCount());
    }

    @Test
    public void should_clean_up_worlds_after_dispose() {
        backend.registerWorld(new MethodClosure(this, "worldClosureCall"));
        backend.buildWorld();

        backend.disposeWorld();

        assertNull(backend.getGroovyWorld());
    }

    @SuppressWarnings("UnusedDeclaration")
    private AnotherCustomWorld worldClosureCall() {
        return new AnotherCustomWorld();
    }
}
