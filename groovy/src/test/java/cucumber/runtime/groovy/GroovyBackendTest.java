package cucumber.runtime.groovy;

import cucumber.runtime.CucumberException;
import cucumber.runtime.io.ResourceLoader;
import groovy.lang.Closure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class GroovyBackendTest {
    @Mock
    ResourceLoader resourceLoader;
    @Mock
    Closure closure;

    GroovyBackend backend;

    @Before
    public void setUp() throws Exception {
        backend = new GroovyBackend(resourceLoader);
    }

    @Test
    public void builds_world_by_calling_closure() {
        backend.registerWorld(closure);
        backend.buildWorld();

        verify(closure).call();
    }

    @Test
    public void builds_default_wold_if_wold_closer_does_not_set() {
        backend.buildWorld();
    }

    @Test(expected = CucumberException.class)
    public void raises_exception_for_two_wolds() {
        backend.registerWorld(closure);
        backend.registerWorld(closure);
    }
}
