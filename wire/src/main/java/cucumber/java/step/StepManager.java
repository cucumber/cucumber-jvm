package cucumber.java.step;

import cucumber.java.MatchResult;

import java.util.HashMap;
import java.util.Map;

public class StepManager {
    private static Map<Integer, StepInfo> steps = new HashMap<Integer, StepInfo>();

    public static void addStep(StepInfo stepInfo) {
        steps.put(stepInfo.id, stepInfo);
    }

    public MatchResult stepMatches(String stepDescription) {
        MatchResult matchResult = new MatchResult();
        for (StepInfo stepInfo : steps.values()) {
            SingleStepMatch currentMatch = stepInfo.matches(stepDescription);
            if (currentMatch.stepInfo != null) {
                matchResult.addMatch(currentMatch);
            }
        }
        return matchResult;
    }

    public StepInfo getStep(int id) {
        return steps.get(id);
    }

    protected Map<Integer, StepInfo> steps() {
        return steps;
    }
}
