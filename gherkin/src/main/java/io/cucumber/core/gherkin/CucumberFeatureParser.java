package io.cucumber.core.gherkin;

import java.net.URI;

public interface CucumberFeatureParser {

    CucumberFeature parse(URI path, String source);

    String version();

}
