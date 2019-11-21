package io.cucumber.core.gherkin;

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

    String getId();
}
