package cucumber.api;

import gherkin.pickles.PickleTag;

import java.util.List;

public interface TestCase {
    int getLine();

    String getName();

    String getScenarioDesignation();

    List<PickleTag> getTags();

    List<Step> getTestSteps();

    String getUri();
}
