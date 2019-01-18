package io.cucumber.core.model;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class FeatureParser {
    private FeatureParser() {

    }

    public static CucumberFeature parseResource(Resource resource) {
        requireNonNull(resource);
        String path = resource.getPath();
        String source = read(resource);

        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(source, matcher);
            List<PickleEvent> pickleEvents = compilePickles(gherkinDocument, resource);
            return new CucumberFeature(gherkinDocument, path, source, pickleEvents);
        } catch (ParserException e) {
            throw new CucumberException(e);
        }
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }


    private static List<PickleEvent> compilePickles(GherkinDocument gherkinDocument, Resource resource) {
        if (gherkinDocument.getFeature() == null) {
            return Collections.emptyList();
        }
        List<PickleEvent> pickleEvents = new ArrayList<>();
        for (Pickle pickle : new Compiler().compile(gherkinDocument)) {
            pickleEvents.add(new PickleEvent(resource.getPath(), pickle));
        }
        return pickleEvents;
    }
}
