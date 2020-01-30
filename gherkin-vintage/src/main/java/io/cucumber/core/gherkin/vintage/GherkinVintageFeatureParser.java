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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class GherkinVintageFeatureParser implements FeatureParser {
    private static Optional<Feature> parseGherkin5(URI path, String source) {
        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(source, matcher);
            if(gherkinDocument.getFeature() == null){
                return Optional.empty();
            }
            List<Pickle> pickles = compilePickles(path, gherkinDocument);
            if (pickles.isEmpty()) {
                return Optional.empty();
            }
            GherkinVintageFeature feature = new GherkinVintageFeature(gherkinDocument, path, source, pickles);
            return Optional.of(feature);
        } catch (ParserException e) {
            throw new FeatureParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    private static List<Pickle> compilePickles(URI path, GherkinDocument document) {
        GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        return new Compiler().compile(document)
            .stream()
            .map(pickle -> {
                return new GherkinVintagePickle(pickle, path, document, dialect);
            })
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Feature> parse(URI path, String source, Supplier<UUID> idGenerator) {
        return parseGherkin5(path, source);
    }

    @Override
    public String version() {
        return "5";
    }
}
