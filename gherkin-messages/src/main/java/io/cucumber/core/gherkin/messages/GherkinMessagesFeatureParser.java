package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParser;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.gherkin.Gherkin;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument;

import java.net.URI;
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
        List<Envelope> sources = singletonList(
            makeSourceEnvelope(source, path.toString()));

        List<Envelope> envelopes = Gherkin.fromSources(
            sources,
            true,
            true,
            true,
            () -> idGenerator.get().toString()).collect(toList());

        GherkinDocument gherkinDocument = envelopes.stream()
                .filter(Envelope::hasGherkinDocument)
                .map(Envelope::getGherkinDocument)
                .findFirst()
                .orElse(null);

        if (gherkinDocument == null || !gherkinDocument.hasFeature()) {
            List<String> errors = envelopes.stream()
                    .filter(Envelope::hasParseError)
                    .map(Envelope::getParseError)
                    .map(Messages.ParseError::getMessage)
                    .collect(toList());
            if (!errors.isEmpty()) {
                throw new FeatureParserException(
                    "Failed to parse resource at: " + path.toString() + "\n" + String.join("\n", errors));
            }
            return Optional.empty();
        }

        CucumberQuery cucumberQuery = new CucumberQuery();
        cucumberQuery.update(gherkinDocument);
        GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        GherkinDocument.Feature feature = gherkinDocument.getFeature();
        String language = feature.getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);

        List<Messages.Pickle> pickleMessages = envelopes.stream()
                .filter(Envelope::hasPickle)
                .map(Envelope::getPickle)
                .collect(toList());

        List<Pickle> pickles = pickleMessages.stream()
                .map(pickle -> new GherkinMessagesPickle(pickle, path, dialect, cucumberQuery))
                .collect(toList());

        GherkinMessagesFeature messagesFeature = new GherkinMessagesFeature(
            feature,
            path,
            source,
            pickles,
            envelopes);
        return Optional.of(messagesFeature);
    }

    @Override
    public String version() {
        return "8";
    }

}
