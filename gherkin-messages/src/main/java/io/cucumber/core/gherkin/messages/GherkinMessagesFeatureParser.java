package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParser;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.gherkin.Gherkin;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.ParserException;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.cucumber.gherkin.Gherkin.makeSourceEnvelope;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public final class GherkinMessagesFeatureParser implements FeatureParser {

    @Override
    public Optional<Feature> parse(URI path, String source, Supplier<UUID> idGenerator) {
        try {

            List<Envelope> sources = singletonList(
                makeSourceEnvelope(source, path.toString())
            );

            List<Envelope> envelopes = Gherkin.fromSources(
                sources,
                true,
                true,
                true,
                () -> idGenerator.get().toString()
            ).collect(toList());

            GherkinDocument gherkinDocument = envelopes.stream()
                .filter(Envelope::hasGherkinDocument)
                .map(Envelope::getGherkinDocument)
                .findFirst()
                .orElse(null);

            if (gherkinDocument == null || !gherkinDocument.hasFeature()) {
                return Optional.empty();
            }

            CucumberQuery cucumberQuery = new CucumberQuery();
            cucumberQuery.update(gherkinDocument);
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            String language = gherkinDocument.getFeature().getLanguage();
            GherkinDialect dialect = dialectProvider.getDialect(language, null);

            List<Messages.Pickle> pickleMessages = envelopes.stream()
                .filter(Envelope::hasPickle)
                .map(Envelope::getPickle)
                .collect(toList());

            if (pickleMessages.isEmpty()) {
                return Optional.empty();
            }

            List<Pickle> pickles = pickleMessages.stream()
                .map(pickle -> new GherkinMessagesPickle(pickle, path, dialect, cucumberQuery))
                .collect(toList());

            GherkinMessagesFeature feature = new GherkinMessagesFeature(
                gherkinDocument,
                path,
                source,
                pickles,
                envelopes
            );
            return Optional.of(feature);
        } catch (ParserException e) {
            throw new FeatureParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    @Override
    public String version() {
        return "8";
    }
}
