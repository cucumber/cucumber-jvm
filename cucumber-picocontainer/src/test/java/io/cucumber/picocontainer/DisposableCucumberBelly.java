package io.cucumber.picocontainer;

import org.picocontainer.Disposable;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A test helper class which simulates a class that holds system resources which
 * need disposing at the end of the test.
 * <p>
 * In a real app, this could be a database connector or similar.
 */
public class DisposableCucumberBelly
        implements Disposable, Startable {
    static final List<String> events = new ArrayList<>();

    private List<String> contents;
    private boolean isDisposed = false;
    private boolean wasStarted = false;
    private boolean wasStopped = false;

    public List<String> getContents() {
        assertFalse(isDisposed);
        return contents;
    }

    public void setContents(List<String> contents) {
        assertFalse(isDisposed);
        this.contents = contents;
    }

    /**
     * "dispose()" is useful in addition to @After, as it is guaranteed to run
     * after all @After hooks, which is useful if this class is needed by the
     * After hooks themselves.
     */
    @Override
    public void dispose() {
        events.add("Disposed");
        isDisposed = true;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void start() {
        events.add("Started");
        wasStarted = true;
    }

    public boolean wasStarted() {
        return wasStarted;
    }

    @Override
    public void stop() {
        events.add("Stopped");
        wasStopped = true;
    }

    public boolean wasStopped() {
        return wasStopped;
    }
}
