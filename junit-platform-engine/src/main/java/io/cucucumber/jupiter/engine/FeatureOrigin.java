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
import java.util.List;

abstract class FeatureOrigin {

    static final String FEATURE_SEGMENT_TYPE = "feature";
    private static final String SCENARIO_SEGMENT_TYPE = "scenario";
    private static final String OUTLINE_SEGMENT_TYPE = "outline";
    private static final String EXAMPLE_SEGMENT_TYPE = "example";
    static final String CLASSPATH_PREFIX = "classpath:";
    static final String FILE_PREFIX = "file:";
    static final String URI_PREFIX = "uri:";


    static FeatureOrigin fromFileResource() {
        return new FileFeatureOrigin();
    }

    private static FilePosition getPickleLocation(PickleLocation location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static PickleLocation getOutlineLocation(PickleEvent firstPickle) {
        List<PickleLocation> locations = firstPickle.pickle.getLocations();
        return locations.get(locations.size() - 1);
    }

    static FeatureOrigin fromClassPathResource() {
        return new ClasspathFeatureOrigin();
    }

    static FeatureOrigin fromSegment(UniqueId.Segment segment) {
        if (segment.getValue().startsWith(CLASSPATH_PREFIX)) {
            return fromClassPathResource();
        }

        return fromFileResource();
    }

    abstract TestSource featureSource(CucumberFeature feature);


    abstract TestSource scenarioSource(PickleEvent pickleEvent);

    abstract TestSource outlineSource(List<PickleEvent> pickleEvents);

    abstract TestSource exampleSource(PickleEvent pickleEvent);

    abstract UniqueId featureSegment(UniqueId parent, CucumberFeature feature);

    UniqueId scenarioSegment(UniqueId parent, PickleEvent pickle) {
        PickleLocation location = pickle.pickle.getLocations().get(0);
        return parent.append(SCENARIO_SEGMENT_TYPE, String.valueOf(location.getLine()));
    }

    UniqueId outlineSegment(UniqueId parent, List<PickleEvent> pickles) {
        PickleEvent firstPickle = pickles.get(0);
        PickleLocation location = FeatureOrigin.getOutlineLocation(firstPickle);
        return parent.append(OUTLINE_SEGMENT_TYPE, String.valueOf(location.getLine()));
    }

    UniqueId exampleSegment(UniqueId parent, PickleEvent pickle) {
        List<PickleLocation> locations = pickle.pickle.getLocations();
        PickleLocation location = locations.get(0);
        return parent.append(EXAMPLE_SEGMENT_TYPE, String.valueOf(location.getLine()));
    }

    abstract String toFeaturePath(UniqueId.Segment segment);

    private static class FileFeatureOrigin extends FeatureOrigin {


        @Override
        TestSource featureSource(CucumberFeature feature) {
            return FileSource.from(new File(feature.getUri()));
        }

        @Override
        TestSource scenarioSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return FileSource.from(new File(pickleEvent.uri), getPickleLocation(locations.get(0)));
        }

        @Override
        TestSource outlineSource(List<PickleEvent> pickleEvents) {
            PickleEvent firstPickle = pickleEvents.get(0);
            PickleLocation location = getOutlineLocation(firstPickle);
            return FileSource.from(new File(firstPickle.uri), getPickleLocation(location));
        }

        @Override
        TestSource exampleSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return FileSource.from(new File(pickleEvent.uri), getPickleLocation(locations.get(0)));
        }

        @Override
        UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, FILE_PREFIX + feature.getUri());
        }

        @Override
        String toFeaturePath(UniqueId.Segment segment) {
            return segment.getValue().substring(FILE_PREFIX.length());
        }
    }

    private static class ClasspathFeatureOrigin extends FeatureOrigin {

        @Override
        TestSource featureSource(CucumberFeature feature) {
            return ClasspathResourceSource.from(feature.getUri());
        }

        @Override
        TestSource scenarioSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return ClasspathResourceSource.from(pickleEvent.uri, getPickleLocation(locations.get(0)));
        }

        @Override
        TestSource outlineSource(List<PickleEvent> pickleEvents) {
            PickleEvent firstPickle = pickleEvents.get(0);
            PickleLocation location = getOutlineLocation(firstPickle);
            return ClasspathResourceSource.from(firstPickle.uri, getPickleLocation(location));
        }

        @Override
        TestSource exampleSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return ClasspathResourceSource.from(pickleEvent.uri, getPickleLocation(locations.get(0)));
        }

        @Override
        UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, CLASSPATH_PREFIX + feature.getUri());
        }

        @Override
        String toFeaturePath(UniqueId.Segment segment) {
            return segment.getValue();
        }
    }
}
