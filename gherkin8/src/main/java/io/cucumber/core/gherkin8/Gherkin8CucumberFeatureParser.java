package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberFeatureParser;
import io.cucumber.core.gherkin.CucumberParserException;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinDocumentBuilder;
import io.cucumber.gherkin.IdGenerator;
import io.cucumber.gherkin.Parser;
import io.cucumber.gherkin.ParserException;
import io.cucumber.gherkin.pickles.PickleCompiler;
import io.cucumber.messages.Messages.GherkinDocument;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Gherkin8CucumberFeatureParser implements CucumberFeatureParser {

    private final CucumberQuery cucumberQuery = new CucumberQuery();

    private static List<CucumberPickle> compilePickles(GherkinDocument document, GherkinDialectProvider dialectProvider, URI path, CucumberQuery cucumberQuery) {
        if (document.getFeature() == null) {
            return Collections.emptyList();
        }
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        IdGenerator idGenerator = new IdGenerator.UUID();

        // TODO: Create a CucumberQuery object here...

        return new PickleCompiler(idGenerator).compile(document, path.toString())
            .stream()
            .map(pickle -> new Gherkin8CucumberPickle(pickle, path, dialect, cucumberQuery))
            .collect(Collectors.toList());
    }

    @Override
    public CucumberFeature parse(URI path, String source) {
        try {
            IdGenerator idGenerator = new IdGenerator.UUID();
            Parser<GherkinDocument.Builder> parser = new Parser<>(new GherkinDocumentBuilder(idGenerator));
            GherkinDocument gherkinDocument = parser.parse(source).setUri(path.toString()).build();
            cucumberQuery.update(gherkinDocument);
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            List<CucumberPickle> pickles = compilePickles(gherkinDocument, dialectProvider, path, cucumberQuery);
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
