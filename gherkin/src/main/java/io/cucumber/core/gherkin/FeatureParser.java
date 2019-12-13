package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

public interface FeatureParser {

    Feature parse(URI path, String source, Supplier<UUID> idGenerator);

    String version();

}
