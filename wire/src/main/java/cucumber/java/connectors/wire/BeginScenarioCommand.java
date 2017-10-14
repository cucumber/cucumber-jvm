package cucumber.java.connectors.wire;

import cucumber.java.CukeEngine;

import java.util.List;

public class BeginScenarioCommand extends ScenarioCommand {
    public BeginScenarioCommand(List<String> tags) {
        super(tags);
    }

    public WireResponse run(CukeEngine engine) throws Throwable {
        engine.beginScenario(tags);
        return new SuccessResponse();
    }
}
