package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public interface FeatureParser {

    Optional<Feature> parse(URI path, String source, Supplier<UUID> idGenerator);

    String version();

}
