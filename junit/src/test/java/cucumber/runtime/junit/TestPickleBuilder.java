package cucumber.runtime.junit;

import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.Pickle;
import cucumber.runtime.model.CucumberFeature;
import gherkin.GherkinDocumentBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.pickles.PickleCompiler;

import java.util.ArrayList;
import java.util.List;

public class TestPickleBuilder {

    private TestPickleBuilder() {
    }

    static List<Pickle> picklesFromFeature(final String path, final String source) {
        PickleCompiler compiler = new PickleCompiler();

        CucumberFeature feature = parseFeature(path, source);
        return new ArrayList<>(compiler.compile(feature.getGherkinFeature(), feature.getUri()));
    }

    static CucumberFeature parseFeature(final String path, final String source) {
        Parser<GherkinDocument.Builder> parser = new Parser<>(new GherkinDocumentBuilder());
        TokenMatcher matcher = new TokenMatcher();

        GherkinDocument gherkinDocument = parser.parse(source, matcher).setUri(path).build();
        return new CucumberFeature(gherkinDocument, source);
    }

}
