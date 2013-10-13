package cucumber.runtime.android;

import android.os.Debug;

/**
 * Waits for dependent components.
 */
public final class Waiter {

    /**
     * The arguments to work with.
     */
    private final Arguments arguments;

    public Waiter(final Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Waits until a debugger is attached.
     */
    public void requestWaitForDebugger() {
        if (arguments.isDebugEnabled()) {
            Debug.waitForDebugger();
        }
    }
}
