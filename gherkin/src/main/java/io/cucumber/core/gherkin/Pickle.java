package io.cucumber.core.gherkin;

import io.cucumber.plugin.event.Location;

import java.net.URI;
import java.util.List;

public interface Pickle {

    String getKeyword();

    String getLanguage();

    String getName();

    /**
     * Returns the location in feature file of the Scenario this pickle was
     * created from. If this pickle was created from a Scenario Outline this
     * location is the location in the Example section used to fill in the place
     * holders.
     *
     * @return location in the feature file
     */
    Location getLocation();

    /**
     * Returns the location in feature file of the Scenario this pickle was
     * created from. If this pickle was created from a Scenario Outline this
     * location is that of the Scenario
     *
     * @return location in the feature file
     */
    Location getScenarioLocation();

    List<Step> getSteps();

    List<String> getTags();

    URI getUri();

    String getId();

}
