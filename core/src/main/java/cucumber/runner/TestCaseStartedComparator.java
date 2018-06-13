package cucumber.runner;

import cucumber.api.event.TestCaseStarted;

import java.util.Comparator;

public class TestCaseStartedComparator implements Comparator<TestCaseStarted> {
    @Override
    public int compare(final TestCaseStarted case1, final TestCaseStarted case2) {
        final int uriCompare = case1.testCase.getUri().compareTo(case1.testCase.getUri());
        if (uriCompare != 0) {
            return uriCompare;
        }
        return new Integer(case1.testCase.getLine()).compareTo(case2.testCase.getLine());
    }
}
