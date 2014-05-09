package cucumber.runtime.groovy

import cucumber.runtime.io.ResourceLoader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@SuppressWarnings("GroovyAccessibility")
@RunWith(MockitoJUnitRunner.class)
public class GroovyBackendTest {
    @Mock
    ResourceLoader resourceLoader

    GroovyBackend backend

    @Before
    void setUp() throws Exception {
        backend = new GroovyBackend(resourceLoader)
    }

    @Test
    void "should build world by calling the closure"() {
        backend.registerWorld({ new AnotherCustomWorld() })
        backend.buildWorld()

        backend.invoke({
            aMethod()
        }, [] as Object[])
    }

    @Test
    void "should build world object even if closure world was not added" () {
        assert !backend.world

        backend.buildWorld()

        assert backend.world
    }

    @Test
    void "should clean up Worlds after dispose"() {
        backend.registerWorld({});

        backend.worldClosures
        backend.disposeWorld()

        assert !backend.world
        assert backend.worldClosures.size() == 1
    }
}
