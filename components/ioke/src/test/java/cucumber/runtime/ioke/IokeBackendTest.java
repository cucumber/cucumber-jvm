package cucumber.runtime.ioke;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class IokeBackendTest extends AbstractBackendTest {
    protected Backend backend() throws IOException {
        return new IokeBackend("cucumber/runtime/ioke");
    }
}
