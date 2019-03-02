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
import java.net.URI;
import java.util.List;

abstract class FeatureOrigin {

    static final String FEATURE_SEGMENT_TYPE = "feature";
    private static final String SCENARIO_SEGMENT_TYPE = "scenario";
    private static final String OUTLINE_SEGMENT_TYPE = "outline";
    private static final String EXAMPLE_SEGMENT_TYPE = "example";
    private static final String CLASSPATH_SCHEME = "classpath";
    static final String CLASSPATH_PREFIX = CLASSPATH_SCHEME + ":";

    private static FeatureOrigin fromFileResource() {
        return new FileFeatureOrigin();
    }

    private static FilePosition getPickleLocation(PickleLocation location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static PickleLocation getOutlineLocation(PickleEvent firstPickle) {
        List<PickleLocation> locations = firstPickle.pickle.getLocations();
        return locations.get(locations.size() - 1);
    }

    private static FeatureOrigin fromClassPathResource() {
        return new ClasspathFeatureOrigin();
    }

    static FeatureOrigin fromUri(URI uri) {
        if (CLASSPATH_SCHEME.equals(uri.getScheme())) {
            return fromClassPathResource();
        }

        return fromFileResource();
    }

    static boolean isClassPath(URI uri) {
        return CLASSPATH_SCHEME.equals(uri.getScheme());
    }

    static boolean isFeatureSegment(UniqueId.Segment segment) {
        return FEATURE_SEGMENT_TYPE.equals(segment.getType());
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
            return parent.append(FEATURE_SEGMENT_TYPE, feature.getUri().toString());
        }

    }

    private static class ClasspathFeatureOrigin extends FeatureOrigin {

        @Override
        TestSource featureSource(CucumberFeature feature) {
            return ClasspathResourceSource.from(classPathUri(feature));
        }

        @Override
        TestSource scenarioSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return ClasspathResourceSource.from(classpathUri(pickleEvent), getPickleLocation(locations.get(0)));
        }

        @Override
        TestSource outlineSource(List<PickleEvent> pickleEvents) {
            PickleEvent firstPickle = pickleEvents.get(0);
            PickleLocation location = getOutlineLocation(firstPickle);
            return ClasspathResourceSource.from(classpathUri(firstPickle), getPickleLocation(location));
        }

        @Override
        TestSource exampleSource(PickleEvent pickleEvent) {
            List<PickleLocation> locations = pickleEvent.pickle.getLocations();
            return ClasspathResourceSource.from(classpathUri(pickleEvent), getPickleLocation(locations.get(0)));
        }

        private String classpathUri(PickleEvent pickleEvent) {
            return classpathUri(pickleEvent.uri);
        }

        private String classpathUri(String uri) {
            return uri.substring(CLASSPATH_PREFIX.length());
        }

        private String classPathUri(CucumberFeature feature) {
            return feature.getUri().getSchemeSpecificPart();
        }

        @Override
        UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, feature.getUri().toString());
        }
    }

}
