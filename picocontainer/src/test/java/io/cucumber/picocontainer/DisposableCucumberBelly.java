package io.cucumber.picocontainer;

import org.picocontainer.Disposable;

import java.util.List;

/**
 * A test helper class which simulates a class that holds system resources which
 * need disposing at the end of the test.
 * <p>
 * In a real app, this could be a database connector or similar.
 */
public class DisposableCucumberBelly
        implements Disposable {

    private List<String> contents;
    private boolean isDisposed = false;

    public List<String> getContents() {
        assert !isDisposed;
        return contents;
    }

    public void setContents(List<String> contents) {
        assert !isDisposed;
        this.contents = contents;
    }

    /**
     * "dispose()" is useful in addition to @After, as it is guaranteed to run
     * after all @After hooks, which is useful if this class is needed by the
     * After hooks themselves.
     */
    @Override
    public void dispose() {
        isDisposed = true;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

}
