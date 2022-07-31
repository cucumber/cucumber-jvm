package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@API(status = API.Status.STABLE)
public interface TestCase {

    /**
     * Returns the line of this Scenario in the feature file. If this Scenario
     * is an example in a Scenario Outline the method wil return the line of the
     * example.
     *
     * @return the line of this scenario.
     */
    @Deprecated
    Integer getLine();

    /**
     * Returns the location of this Scenario in the feature file. If this
     * Scenario is an example in a Scenario Outline the method wil return the
     * location of the example.
     *
     * @return the location of this scenario.
     */
    Location getLocation();

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
