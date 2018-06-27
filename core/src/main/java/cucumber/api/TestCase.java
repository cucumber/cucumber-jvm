package cucumber.api;

import io.cucumber.messages.Messages.PickleTag;

import java.util.List;

public interface TestCase {
    int getLine();

    String getName();

    String getScenarioDesignation();

    List<PickleTag> getTags();

    List<TestStep> getTestSteps();

    String getUri();
}
