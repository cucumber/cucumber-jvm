package cucumber.runtime.junit;

import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureParser;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class TestPickleBuilder {

    private TestPickleBuilder() {
    }

    static List<PickleEvent> pickleEventsFromFeature(final String path, final String source) {
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        Compiler compiler = new Compiler();

        CucumberFeature feature = parseFeature(path, source);
        for (Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(feature.getUri(), pickle));
        }

        return pickleEvents;
    }

    static CucumberFeature parseFeature(final String path, final String source) {
        return FeatureParser.parseResource(new Resource() {
            @Override
            public String getPath() {
                return path;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes());
            }

        });
    }
}
