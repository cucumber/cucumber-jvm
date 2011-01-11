package cucumber.runtime.rhino;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class RhinoBackendTest extends AbstractBackendTest {
    protected Backend backend() throws IOException {
        return new RhinoBackend("cucumber/runtime/rhino");
    }
}
