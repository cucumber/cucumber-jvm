package io.cucumber.core.gherkin.vintage;

import gherkin.AstBuilder;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParser;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.gherkin.Pickle;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class GherkinVintageFeatureParser implements FeatureParser {
    private static Feature parseGherkin5(URI path, String source) {
        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(source, matcher);
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            List<Pickle> pickles = compilePickles(gherkinDocument, dialectProvider, path);
            return new GherkinVintageFeature(gherkinDocument, path, source, pickles);
        } catch (ParserException e) {
            throw new FeatureParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    private static List<Pickle> compilePickles(GherkinDocument document, GherkinDialectProvider dialectProvider, URI path) {
        if (document.getFeature() == null) {
            return Collections.emptyList();
        }
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        return new Compiler().compile(document)
            .stream()
            .map(pickle -> new GherkinVintagePickle(pickle, path, document, dialect))
            .collect(Collectors.toList());
    }

    @Override
    public Feature parse(URI path, String source, Supplier<UUID> idGenerator) {
        return parseGherkin5(path, source);
    }

    @Override
    public String version() {
        return "5";
    }
}
