package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@API(status = API.Status.STABLE)
public interface TestCase {

    /**
     * @return the line in the feature file of the Scenario. If this is a Scenario
     * from Scenario Outlines this wil return the line of the example row in
     * the Scenario Outline.
     */
    Integer getLine();

    String getKeyword();

    String getName();

    /**
     * @deprecated use other accessor to reconstruct the scenario designation
     */
    @Deprecated
    String getScenarioDesignation();

    List<String> getTags();

    List<TestStep> getTestSteps();

    URI getUri();

    UUID getId();
}
