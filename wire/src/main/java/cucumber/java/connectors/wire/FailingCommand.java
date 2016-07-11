package cucumber.java.connectors.wire;

import cucumber.java.CukeEngine;

public class FailingCommand implements WireCommand {
    public WireResponse run(CukeEngine engine) {
        return new FailureResponse();
    }
}
