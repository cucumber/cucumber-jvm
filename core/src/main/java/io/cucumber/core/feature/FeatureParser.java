package io.cucumber.core.feature;

import gherkin.AstBuilder;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class FeatureParser {
    private FeatureParser() {

    }

    public static CucumberFeature parseResource(Resource resource) {
        requireNonNull(resource);
        URI path = resource.getPath();
        String source = read(resource);

        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(source, matcher);
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            List<CucumberPickle> pickles = compilePickles(gherkinDocument, dialectProvider, resource);
            return new CucumberFeature(gherkinDocument, path, source, pickles);
        } catch (ParserException e) {
            throw new CucumberException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }


    private static List<CucumberPickle> compilePickles(GherkinDocument document, GherkinDialectProvider dialectProvider, Resource resource) {
        if (document.getFeature() == null) {
            return Collections.emptyList();
        }
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        return new Compiler().compile(document)
            .stream()
            .map(pickle -> new CucumberPickle(pickle, resource.getPath(), document, dialect))
            .collect(Collectors.toList());
    }
}
