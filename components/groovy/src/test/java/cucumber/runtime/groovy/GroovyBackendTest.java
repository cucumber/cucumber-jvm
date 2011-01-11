package cucumber.runtime.groovy;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class GroovyBackendTest extends AbstractBackendTest {
    protected Backend backend() throws IOException {
        return new GroovyBackend("cucumber/runtime/groovy");
    }
}
