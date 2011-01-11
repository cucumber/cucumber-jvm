package cucumber.runtime.java;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public abstract class JavaBackendTest extends AbstractBackendTest {
    protected Backend backend() throws IOException {
        return new JavaBackend("cucumber.runtime.java");
    }
}
