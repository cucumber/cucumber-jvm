package cucumber.java.connectors.wire;

import cucumber.java.StepMatch;

import java.util.List;

public class StepMatchesResponse implements WireResponse {
    private List<StepMatch> matchingSteps;

    public StepMatchesResponse(List<StepMatch> matchingSteps) {
        this.matchingSteps = matchingSteps;
    }

    public List<StepMatch> getMatchingSteps() { return matchingSteps; }
}
