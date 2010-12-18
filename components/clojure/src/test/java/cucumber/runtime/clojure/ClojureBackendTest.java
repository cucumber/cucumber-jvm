package cucumber.runtime.clojure;

import cucumber.runtime.AbstractBackendTest;
import cucumber.runtime.Backend;

import java.io.IOException;

public class ClojureBackendTest extends AbstractBackendTest {
    @Override
    protected String expectedOutput() {
        return "JALLA";
    }

    @Override
    protected Backend backend() throws IOException {
        return new ClojureBackend("cucumber/runtime/clojure");
    }
}
