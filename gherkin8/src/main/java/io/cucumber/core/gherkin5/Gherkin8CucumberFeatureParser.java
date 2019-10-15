package io.cucumber.core.gherkin5;

import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberFeatureParser;
import io.cucumber.core.gherkin.CucumberParserException;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.gherkin.Gherkin;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinDocumentBuilder;
import io.cucumber.gherkin.Parser;
import io.cucumber.gherkin.ParserException;
import io.cucumber.gherkin.pickles.PickleCompiler;
import io.cucumber.messages.Messages;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Gherkin8CucumberFeatureParser implements CucumberFeatureParser {
    private static CucumberFeature parseGherkin5(URI path, String source) {
        try {

            Stream<Messages.Envelope> messages = Gherkin.fromStream(new ByteArrayInputStream(source.getBytes(UTF_8)));

            Parser<Messages.GherkinDocument.Builder> parser = new Parser<>(new GherkinDocumentBuilder());
            Messages.GherkinDocument gherkinDocument = parser.parse(source).setUri(path.toString()).build();
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            List<CucumberPickle> pickles = compilePickles(source, gherkinDocument, dialectProvider, path);
            return new Gherkin8CucumberFeature(gherkinDocument, path, source, pickles);
        } catch (ParserException e) {
            throw new CucumberParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    private static List<CucumberPickle> compilePickles(String source, Messages.GherkinDocument document, GherkinDialectProvider dialectProvider, URI path) {
        if (document.getFeature() == null) {
            return Collections.emptyList();
        }
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        return new PickleCompiler().compile(document, path.toString(), source)
            .stream()
            .map(pickle -> new Gherkin8CucumberPickle(pickle, path, document, dialect))
            .collect(Collectors.toList());
    }

    @Override
    public CucumberFeature parse(URI path, String source) {
        return parseGherkin5(path, source);
    }

    @Override
    public String version() {
        return "8";
    }
}
