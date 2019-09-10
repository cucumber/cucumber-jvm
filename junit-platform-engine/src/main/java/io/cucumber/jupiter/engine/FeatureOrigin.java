package io.cucumber.jupiter.engine;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
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

    private static FilePosition getPickleLocation(CucumberPickle location) {
        return FilePosition.from(location.getLine());
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

    abstract TestSource scenarioSource(CucumberPickle pickleEvent);

    abstract TestSource outlineSource(List<CucumberPickle> pickleEvents);

    abstract TestSource exampleSource(CucumberPickle pickleEvent);

    abstract UniqueId featureSegment(UniqueId parent, CucumberFeature feature);

    UniqueId scenarioSegment(UniqueId parent, CucumberPickle pickle) {
        return parent.append(SCENARIO_SEGMENT_TYPE, String.valueOf(pickle.getLine()));
    }

    UniqueId outlineSegment(UniqueId parent, List<CucumberPickle> pickles) {
        CucumberPickle firstPickle = pickles.get(0);
        return parent.append(OUTLINE_SEGMENT_TYPE, String.valueOf(firstPickle.getScenarioLine()));
    }

    UniqueId exampleSegment(UniqueId parent, CucumberPickle pickle) {
        return parent.append(EXAMPLE_SEGMENT_TYPE, String.valueOf(pickle.getLine()));
    }

    private static class FileFeatureOrigin extends FeatureOrigin {

        @Override
        TestSource featureSource(CucumberFeature feature) {
            return FileSource.from(new File(feature.getUri()));
        }

        @Override
        TestSource scenarioSource(CucumberPickle pickleEvent) {
            return FileSource.from(new File(pickleEvent.getUri()), getPickleLocation(pickleEvent));
        }

        @Override
        TestSource outlineSource(List<CucumberPickle> pickleEvents) {
            CucumberPickle firstPickle = pickleEvents.get(0);
            return FileSource.from(new File(firstPickle.getUri()), getPickleLocation(firstPickle));
        }

        @Override
        TestSource exampleSource(CucumberPickle pickleEvent) {
            return FileSource.from(new File(pickleEvent.getUri()), getPickleLocation(pickleEvent));
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
        TestSource scenarioSource(CucumberPickle pickleEvent) {
            return ClasspathResourceSource.from(classpathUri(pickleEvent), getPickleLocation(pickleEvent));
        }

        @Override
        TestSource outlineSource(List<CucumberPickle> pickleEvents) {
            CucumberPickle firstPickle = pickleEvents.get(0);
            return ClasspathResourceSource.from(classpathUri(firstPickle), getPickleLocation(firstPickle));
        }

        @Override
        TestSource exampleSource(CucumberPickle pickleEvent) {
            return ClasspathResourceSource.from(classpathUri(pickleEvent), getPickleLocation(pickleEvent));
        }

        private String classpathUri(CucumberPickle pickleEvent) {
            return classpathUri(pickleEvent.getUri());
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
