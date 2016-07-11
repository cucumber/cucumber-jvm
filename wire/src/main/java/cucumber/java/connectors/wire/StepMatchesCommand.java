package cucumber.java.connectors.wire;

import cucumber.java.CukeEngine;
import cucumber.java.StepMatch;

import java.util.List;

public class StepMatchesCommand implements WireCommand {
    private String name_to_match;

    public StepMatchesCommand(String name_to_match) {
        this.name_to_match = name_to_match;
    }

    public WireResponse run(CukeEngine engine) {
        List<StepMatch> matchingSteps = engine.stepMatches(name_to_match);
        return new StepMatchesResponse(matchingSteps);
    }
}
