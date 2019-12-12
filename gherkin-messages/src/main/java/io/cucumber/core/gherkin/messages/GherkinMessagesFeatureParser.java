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
import io.cucumber.messages.Messages.GherkinDocument;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static io.cucumber.gherkin.Gherkin.makeSourceEnvelope;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public final class GherkinMessagesFeatureParser implements FeatureParser {

    @Override
    public Feature parse(URI path, String source, Supplier<UUID> idGenerator) {
        try {
            CucumberQuery cucumberQuery = new CucumberQuery();

            List<Messages.Envelope> sources = singletonList(
                makeSourceEnvelope(source, path.toString())
            );

            List<Messages.Envelope> envelopes = Gherkin.fromSources(
                sources,
                true,
                true,
                true,
                () -> idGenerator.get().toString()
            ).collect(toList());

            GherkinDialect dialect = null;
            GherkinDocument gherkinDocument = null;
            List<Pickle> pickles = new ArrayList<>();
            for (Messages.Envelope envelope : envelopes) {
                if (envelope.hasGherkinDocument()) {
                    gherkinDocument = envelope.getGherkinDocument();
                    cucumberQuery.update(gherkinDocument);
                    GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
                    String language = gherkinDocument.getFeature().getLanguage();
                    dialect = dialectProvider.getDialect(language, null);
                }
                if (envelope.hasPickle()) {
                    Messages.Pickle pickle = envelope.getPickle();
                    pickles.add(new GherkinMessagesPickle(pickle, path, dialect, cucumberQuery));
                }
            }

            return new GherkinMessagesFeature(gherkinDocument, path, source, pickles, envelopes);
        } catch (ParserException e) {
            throw new FeatureParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    @Override
    public String version() {
        return "8";
    }
}
