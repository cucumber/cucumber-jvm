package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.util.Deque;
import java.util.List;

abstract class FeatureSource implements TestSource {

    static FeatureSource fromFile() {
        return new FileFeatureSource();
    }

    private static FilePosition getPickleLocation(PickleLocation location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static PickleLocation getOutlineLocation(PickleEvent firstPickle) {
        List<PickleLocation> locations = firstPickle.pickle.getLocations();
        return locations.get(locations.size() - 1);
    }

    static FeatureSource fromClassPathResource() {
        return new ClasspathFeatureSource();
    }

    abstract TestSource featureSource(CucumberFeature feature);

    abstract UniqueId featureSegment(UniqueId parent, CucumberFeature feature);

    abstract TestSource scenarioSource(PickleEvent pickle);

    abstract UniqueId scenarioSegment(UniqueId parent, PickleEvent pickle);

    abstract TestSource outlineSource(List<PickleEvent> pickleEvents);

    abstract UniqueId outlineSegment(UniqueId parent, List<PickleEvent> pickles);

    abstract TestSource exampleSource(PickleEvent pickleEvent);

    abstract UniqueId exampleSegment(UniqueId parent, PickleEvent pickle);

    private static class FileFeatureSource extends FeatureSource {

        @Override
        public TestSource featureSource(CucumberFeature feature) {
            return FileSource.from(new File(feature.getUri()));
        }

        @Override
        public UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append("feature", feature.getUri());
        }

        @Override
        public TestSource scenarioSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return FileSource.from(new File(pickleEvent.uri), getPickleLocation(locations.get(0)));
        }

        @Override
        public UniqueId scenarioSegment(UniqueId parent, PickleEvent pickle) {
            PickleLocation location = pickle.pickle.getLocations().get(0);
            return parent.append("scenario", String.valueOf(location.getLine()));
        }

        @Override
        public TestSource outlineSource(List<PickleEvent> pickleEvents) {
            PickleEvent firstPickle = pickleEvents.get(0);
            PickleLocation location = getOutlineLocation(firstPickle);
            return FileSource.from(new File(firstPickle.uri), getPickleLocation(location));
        }

        @Override
        public UniqueId outlineSegment(UniqueId parent, List<PickleEvent> pickles) {
            PickleEvent firstPickle = pickles.get(0);
            PickleLocation location = FeatureSource.getOutlineLocation(firstPickle);
            return parent.append("outline", String.valueOf(location.getLine()));
        }

        @Override
        public TestSource exampleSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return FileSource.from(new File(pickleEvent.uri), getPickleLocation(locations.get(0)));
        }

        @Override
        public UniqueId exampleSegment(UniqueId parent, PickleEvent pickle) {
            List<PickleLocation> locations = pickle.pickle.getLocations();
            PickleLocation location = locations.get(0);
            return parent.append("example", String.valueOf(location.getLine()));
        }

    }

    private static class ClasspathFeatureSource extends FeatureSource {

        @Override
        public TestSource featureSource(CucumberFeature feature) {
            return ClasspathResourceSource.from(feature.getUri());
        }

        @Override
        public UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append("feature", feature.getUri());
        }

        @Override
        public TestSource scenarioSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return ClasspathResourceSource.from(pickleEvent.uri, getPickleLocation(locations.get(0)));
        }

        @Override
        public UniqueId scenarioSegment(UniqueId parent, PickleEvent pickle) {
            List<PickleLocation> locations = pickle.pickle.getLocations();
            PickleLocation location = locations.get(0);
            return parent.append("scenario", String.valueOf(location.getLine()));
        }

        @Override
        public TestSource outlineSource(List<PickleEvent> pickleEvents) {
            PickleEvent firstPickle = pickleEvents.get(0);
            PickleLocation location = getOutlineLocation(firstPickle);
            return ClasspathResourceSource.from(firstPickle.uri, getPickleLocation(location));
        }

        @Override
        public UniqueId outlineSegment(UniqueId parent, List<PickleEvent> pickles) {
            PickleEvent firstPickle = pickles.get(0);
            PickleLocation location = FeatureSource.getOutlineLocation(firstPickle);
            return parent.append("outline", String.valueOf(location.getLine()));
        }

        @Override
        public TestSource exampleSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return ClasspathResourceSource.from(pickleEvent.uri, getPickleLocation(locations.get(0)));
        }

        @Override
        public UniqueId exampleSegment(UniqueId parent, PickleEvent pickle) {
            List<PickleLocation> locations = pickle.pickle.getLocations();
            PickleLocation location = locations.get(0);
            return parent.append("example", String.valueOf(location.getLine()));
        }
    }
}
