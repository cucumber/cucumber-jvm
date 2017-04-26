package cucumber.api.testng;

import cucumber.runtime.model.CucumberTagStatement;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#feature(CucumberFeatureWrapper)
 */
public class CucumberTagStatementWrapperImpl implements CucumberTagStatementWrapper {
    private final CucumberTagStatement cucumberScenario;

    public CucumberTagStatementWrapperImpl(CucumberTagStatement cucumberScenario) {
        this.cucumberScenario = cucumberScenario;
    }

    @Override
    public CucumberTagStatement getCucumberScenario() {
        return cucumberScenario;
    }

    @Override
    public String toString() {

        String returnVal = cucumberScenario.getVisualName();

        if (!cucumberScenario.getVisualName().startsWith("Scenario"))
            returnVal = String.format("Scenario Outline: %s{%s)", cucumberScenario.getGherkinModel().getName(), cucumberScenario.getVisualName());

        return returnVal;
    }
}
