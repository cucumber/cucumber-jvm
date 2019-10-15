package io.cucumber.core.gherkin5;

import gherkin.AstBuilder;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberFeatureParser;
import io.cucumber.core.gherkin.CucumberParserException;
import io.cucumber.core.gherkin.CucumberPickle;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Gherkin5CucumberFeatureParser implements CucumberFeatureParser {
    @Override
    public CucumberFeature parse(URI path, String source) {
        return parseGherkin5(path, source);
    }

    @Override
    public String version() {
        return "5";
    }

    private static CucumberFeature parseGherkin5(URI path, String source) {
        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(source, matcher);
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            List<CucumberPickle> pickles = compilePickles(gherkinDocument, dialectProvider, path);
            return new Gherkin5CucumberFeature(gherkinDocument, path, source, pickles);
        } catch (ParserException e) {
            throw new CucumberParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }


    private static List<CucumberPickle> compilePickles(GherkinDocument document, GherkinDialectProvider dialectProvider, URI path) {
        if (document.getFeature() == null) {
            return Collections.emptyList();
        }
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        return new Compiler().compile(document)
            .stream()
            .map(pickle -> new Gherkin5CucumberPickle(pickle, path, document, dialect))
            .collect(Collectors.toList());
    }
}
