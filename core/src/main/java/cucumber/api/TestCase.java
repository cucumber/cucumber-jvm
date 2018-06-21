package cucumber.api;

import cucumber.messages.Pickles.PickleTag;

import java.util.List;

public interface TestCase {
    int getLine();

    String getName();

    String getScenarioDesignation();

    List<PickleTag> getTags();

    List<TestStep> getTestSteps();

    String getUri();
}
