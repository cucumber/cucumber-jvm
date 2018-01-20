package cucumber.runtime.junit;

import cucumber.runtime.model.CucumberFeature;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import java.util.ArrayList;
import java.util.List;

public class TestPickleBuilder {

    private TestPickleBuilder() {
    }

    static List<PickleEvent> pickleEventsFromFeature(final String path, final String source) {
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        Compiler compiler = new Compiler();

        CucumberFeature feature = parseFeature(path, source);
        for (Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(feature.getUri(), pickle));
        };
        return pickleEvents;
    }

    static CucumberFeature parseFeature(final String path, final String source) {
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();

        GherkinDocument gherkinDocument = parser.parse(source, matcher);
        return new CucumberFeature(gherkinDocument, path, source);
    }

}
