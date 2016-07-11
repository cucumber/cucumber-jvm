package cucumber.java.step;

import cucumber.java.utils.Regex;
import cucumber.java.utils.RegexMatch;

public abstract class StepInfo {
    private static int currentId = 0;

    public int id;
    public Regex regex;
    public String source;

    public StepInfo(String stepMatcher, String source) {
        regex = new Regex(stepMatcher);
        this.source = source;
        id = ++currentId;
    }

    public SingleStepMatch matches(String stepDescription) {
        SingleStepMatch stepMatch = new SingleStepMatch();
        RegexMatch regexMatch = regex.find(stepDescription);
        if (regexMatch.matches()) {
            stepMatch.stepInfo = this;
            stepMatch.submatches = regexMatch.getSubmatches();
        }
        return stepMatch;
    }

    public abstract InvokeResult invokeStep(InvokeArgs args);
}
