package io.cucumber.core.gherkin;

import io.cucumber.plugin.event.Location;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface Pickle {

    String getKeyword();

    String getLanguage();

    String getName();

    /**
     * Returns the location in the feature file of the Scenario this pickle was
     * created from. If this pickle was created from a Scenario Outline this
     * location is the location in the Example section used to fill in the place
     * holders.
     *
     * @return location in the feature file
     */
    Location getLocation();

    /**
     * Returns the location in the feature file of the Scenario this pickle was
     * created from. If this pickle was created from a Scenario Outline this
     * location is that of the Scenario
     *
     * @return location in the feature file
     */
    Location getScenarioLocation();

    /**
     * Returns the location in the feature file of the Rule this pickle was
     * created from.
     *
     * @return location in the feature file
     */
    default Optional<Location> getRuleLocation() {
        return Optional.empty();
    }

    /**
     * Returns the location in the feature file of the Feature this pickle was
     * created from.
     *
     * @return location in the feature file
     */
    default Optional<Location> getFeatureLocation() {
        return Optional.empty();
    }

    /**
     * Returns the location in the feature file of the examples this pickle was
     * created from.
     *
     * @return location in the feature file
     */
    default Optional<Location> getExamplesLocation() {
        return Optional.empty();
    }

    List<Step> getSteps();

    List<String> getTags();

    URI getUri();

    String getId();

}
