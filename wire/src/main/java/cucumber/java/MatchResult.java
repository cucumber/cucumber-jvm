package cucumber.java;

import cucumber.java.step.SingleStepMatch;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {
    private List<SingleStepMatch> resultSet = new ArrayList<SingleStepMatch>();

    public List<SingleStepMatch> getResultSet() {
        return resultSet;
    }

    public void addMatch(SingleStepMatch match) {
        resultSet.add(match);
    }
}
