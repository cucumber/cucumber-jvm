package cucumber.runtime.android;

import android.os.Debug;

/**
 * Waits for the debugger, if configured through the given {@link cucumber.runtime.android.Arguments}.
 */
public final class DebuggerWaiter {

    /**
     * The arguments to work with.
     */
    private final Arguments arguments;

    /**
     * Creates a new instance for the given arguments.
     *
     * @param arguments the {@link cucumber.runtime.android.Arguments} which specify whether waiting is required.
     */
    public DebuggerWaiter(final Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Waits until a debugger is attached, if configured.
     */
    public void requestWaitForDebugger() {
        if (arguments.isDebugEnabled()) {
            Debug.waitForDebugger();
        }
    }
}
