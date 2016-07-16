package cucumber.runtime.junit;

import cucumber.runtime.model.CucumberFeature;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import java.util.ArrayList;
import java.util.List;

public class TestPickleBuilder {

    private TestPickleBuilder() {
    }

    static List<Pickle> picklesFromFeature(final String path, final String source) {
        List<Pickle> pickles = new ArrayList<Pickle>();
        Compiler compiler = new Compiler();

        CucumberFeature feature = parseFeature(path, source);
        pickles.addAll(compiler.compile(feature.getGherkinFeature(), feature.getPath()));
        return pickles;
    }

    static CucumberFeature parseFeature(final String path, final String source) {
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();

        GherkinDocument gherkinDocument = parser.parse(source, matcher);
        return new CucumberFeature(gherkinDocument, path, source);
    }

}
