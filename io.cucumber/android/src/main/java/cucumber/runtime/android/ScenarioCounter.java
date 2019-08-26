package cucumber.runtime.android;

import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;

import java.util.List;

/**
 * Utility class to count scenarios, including outlined.
 */
public final class ScenarioCounter {

    private ScenarioCounter() {
        // disallow public instantiation
    }

    /**
     * Counts the number of test cases for the given {@code cucumberFeatures}.
     *
     * @param cucumberFeatures the list of {@link CucumberFeature} to count the test cases for
     * @return the number of test cases
     */
    public static int countScenarios(final List<CucumberFeature> cucumberFeatures) {
        int numberOfTestCases = 0;
        for (final CucumberFeature cucumberFeature : cucumberFeatures) {
            for (final CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
                if (cucumberTagStatement instanceof CucumberScenario) {
                    numberOfTestCases++;
                } else if (cucumberTagStatement instanceof CucumberScenarioOutline) {
                    for (final CucumberExamples cucumberExamples : ((CucumberScenarioOutline) cucumberTagStatement).getCucumberExamplesList()) {
                        final int numberOfRows = cucumberExamples.getExamples().getRows().size();
                        final int numberOfRowsExcludingHeader = numberOfRows - 1;
                        numberOfTestCases += numberOfRowsExcludingHeader;
                    }
                }
            }
        }
        return numberOfTestCases;
    }
}
