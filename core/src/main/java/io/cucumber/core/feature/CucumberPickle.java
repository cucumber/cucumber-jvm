package io.cucumber.core.feature;

import java.net.URI;
import java.util.List;

public interface CucumberPickle {
    String getLanguage();

    String getName();

    int getLine();

    int getScenarioLine();

    List<CucumberStep> getSteps();

    List<String> getTags();

    URI getUri();
}
