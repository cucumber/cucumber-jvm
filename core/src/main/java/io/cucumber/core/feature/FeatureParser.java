package io.cucumber.core.feature;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public final class FeatureParser {

    private final EncodingParser encodingParser = new EncodingParser();

    private final Supplier<UUID> idGenerator;

    public FeatureParser(Supplier<UUID> idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Optional<Feature> parseResource(Resource resource) {
        requireNonNull(resource);
        URI uri = resource.getUri();

        String source = encodingParser.parse(resource);

        ServiceLoader<io.cucumber.core.gherkin.FeatureParser> services = ServiceLoader
                .load(io.cucumber.core.gherkin.FeatureParser.class);
        Iterator<io.cucumber.core.gherkin.FeatureParser> iterator = services.iterator();
        List<io.cucumber.core.gherkin.FeatureParser> parser = new ArrayList<>();
        while (iterator.hasNext()) {
            parser.add(iterator.next());
        }
        Comparator<io.cucumber.core.gherkin.FeatureParser> version = comparing(
            io.cucumber.core.gherkin.FeatureParser::version);
        return Collections.max(parser, version).parse(uri, source, idGenerator);
    }

}
