package cucumber.java.connectors.wire;

import cucumber.java.CukeEngine;

/**
 * Wire protocol request command.
 */
public interface WireCommand {
    /**
     * Runs the command on the provided engine
     *
     * @param engine
     * @return The command response (ownership passed to the caller)
     */
    WireResponse run(CukeEngine engine) throws Throwable;
}
