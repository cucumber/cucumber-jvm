package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.util.List;

class FeatureSource {

    static TestSource fromPickle(CucumberFeature feature, PickleEvent pickle) {
        File file = new File(feature.getUri());
        PickleLocation pickleLocation = pickle.pickle.getLocations().get(0);
        return FileSource.from(file, FilePosition.from(pickleLocation.getLine(), pickleLocation.getColumn()));
    }

    static TestSource fromOutline(CucumberFeature feature, PickleEvent pickle) {
        List<PickleLocation> locations = pickle.pickle.getLocations();
        PickleLocation scenarioLocation = locations.get(locations.size() - 1);
        return FileSource.from(new File(feature.getUri()), FilePosition.from(scenarioLocation.getLine(), scenarioLocation.getColumn()));
    }


}
