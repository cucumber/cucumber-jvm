package cucumber.java.connectors.wire;

import cucumber.java.CukeEngine;

import java.util.List;

public class EndScenarioCommand extends ScenarioCommand {
    public EndScenarioCommand(List<String> tags) {
        super(tags);
    }

    public WireResponse run(CukeEngine engine) throws Throwable {
        engine.endScenario(tags);
        return new SuccessResponse();
    }
}
