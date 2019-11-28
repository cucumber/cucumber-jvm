package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberFeatureParser;
import io.cucumber.core.gherkin.CucumberParserException;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.gherkin.Gherkin;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.IdGenerator;
import io.cucumber.gherkin.ParserException;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Gherkin8CucumberFeatureParser implements CucumberFeatureParser {

    @Override
    public CucumberFeature parse(URI path, String source) {
        try {
            CucumberQuery cucumberQuery = new CucumberQuery();
            GherkinDocument gherkinDocument = null;
            GherkinDialect dialect = null;
            List<Messages.Envelope> envelopes = Gherkin.fromSources(
                Collections.singletonList(Gherkin.makeSourceEnvelope(source, path.toString())),
                true,
                true,
                true,
                new IdGenerator.UUID()
            ).collect(Collectors.toList());
            List<CucumberPickle> pickles = new ArrayList<>();
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
                    pickles.add(new Gherkin8CucumberPickle(pickle, path, dialect, cucumberQuery));
                }
            }
            return new Gherkin8CucumberFeature(gherkinDocument, path, source, pickles);
        } catch (ParserException e) {
            throw new CucumberParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    @Override
    public String version() {
        return "8";
    }
}
