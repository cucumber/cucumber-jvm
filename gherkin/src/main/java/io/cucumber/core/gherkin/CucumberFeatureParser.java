package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

public interface CucumberFeatureParser {

    CucumberFeature parse(URI path, String source, Supplier<UUID> idGenerator);

    String version();

}
