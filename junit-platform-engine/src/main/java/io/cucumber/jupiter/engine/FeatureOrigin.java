package io.cucumber.jupiter.engine;

import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.descriptor.UriSource;

import java.net.URI;
import java.util.List;

abstract class FeatureOrigin {

    static final String FEATURE_SEGMENT_TYPE = "feature";
    private static final String SCENARIO_SEGMENT_TYPE = "scenario";
    private static final String OUTLINE_SEGMENT_TYPE = "outline";
    private static final String EXAMPLE_SEGMENT_TYPE = "example";
    private static final String CLASSPATH_SCHEME = "classpath";
    static final String CLASSPATH_PREFIX = CLASSPATH_SCHEME + ":";

    private static FilePosition getPickleLocation(CucumberPickle location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }
    private static FilePosition createFilePosition(Location location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static FeatureOrigin fromUri(URI uri) {
        if (CLASSPATH_SCHEME.equals(uri.getScheme())) {
            ClasspathResourceSource source = ClasspathResourceSource.from(uri);
            return new ClasspathFeatureOrigin(source);
        }

        UriSource source = UriSource.from(uri);
        if (source instanceof FileSource) {
            return new FileFeatureOrigin((FileSource) source);
        }

        return new UriFeatureOrigin(source);

    }

    static boolean isClassPath(URI uri) {
        return CLASSPATH_SCHEME.equals(uri.getScheme());
    }

    static boolean isFeatureSegment(UniqueId.Segment segment) {
        return FEATURE_SEGMENT_TYPE.equals(segment.getType());
    }

    abstract TestSource featureSource();

    abstract TestSource scenarioSource(ScenarioDefinition scenarioDefinition);

    abstract TestSource outlineSource(ScenarioOutline scenarioOutline);

    abstract TestSource exampleSource(CucumberPickle pickleEvent);

    abstract UniqueId featureSegment(UniqueId parent, CucumberFeature feature);

    UniqueId scenarioSegment(UniqueId parent, ScenarioDefinition scenarioDefinition) {
        return parent.append(SCENARIO_SEGMENT_TYPE, String.valueOf(scenarioDefinition.getLocation().getLine()));
    }

    UniqueId outlineSegment(UniqueId parent, ScenarioOutline scenarioOutline) {
        return parent.append(OUTLINE_SEGMENT_TYPE, String.valueOf(scenarioOutline.getLocation().getLine()));
    }

    UniqueId exampleSegment(UniqueId parent, CucumberPickle pickle) {
        return parent.append(EXAMPLE_SEGMENT_TYPE, String.valueOf(pickle.getLine()));
    }

    private static class FileFeatureOrigin extends FeatureOrigin {

        private final FileSource source;

        FileFeatureOrigin(FileSource source) {
            this.source = source;
        }

        @Override
        TestSource featureSource() {
            return source;
        }

        @Override
        TestSource scenarioSource(ScenarioDefinition scenarioDefinition) {
            return FileSource.from(source.getFile(), createFilePosition(scenarioDefinition.getLocation()));
        }

        @Override
        TestSource outlineSource(ScenarioOutline scenarioOutline) {
            return FileSource.from(source.getFile(), createFilePosition(scenarioOutline.getLocation()));
        }

        @Override
        TestSource exampleSource(CucumberPickle pickleEvent) {
            return FileSource.from(source.getFile(), getPickleLocation(pickleEvent));
        }

        @Override
        UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, source.getUri().toString());
        }
    }

    private static class UriFeatureOrigin extends FeatureOrigin {

        private final UriSource source;

        UriFeatureOrigin(UriSource source) {
            this.source = source;
        }

        @Override
        TestSource featureSource() {
            return source;
        }

        @Override
        TestSource scenarioSource(ScenarioDefinition scenarioDefinition) {
            return source;
        }

        @Override
        TestSource outlineSource(ScenarioOutline scenarioOutline) {
            return source;
        }

        @Override
        TestSource exampleSource(CucumberPickle pickleEvent) {
            return source;
        }

        @Override
        UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, source.getUri().toString());
        }
    }

    private static class ClasspathFeatureOrigin extends FeatureOrigin {

        private final ClasspathResourceSource source;

        ClasspathFeatureOrigin(ClasspathResourceSource source) {
            this.source = source;
        }

        @Override
        TestSource featureSource() {
            return source;
        }

        @Override
        TestSource scenarioSource(ScenarioDefinition scenarioDefinition) {
            return ClasspathResourceSource.from(source.getClasspathResourceName(), createFilePosition(scenarioDefinition.getLocation()));
        }

        @Override
        TestSource outlineSource(ScenarioOutline scenarioOutline) {
            return ClasspathResourceSource.from(source.getClasspathResourceName(), createFilePosition(scenarioOutline.getLocation()));
        }

        @Override
        TestSource exampleSource(CucumberPickle pickleEvent) {
            return ClasspathResourceSource.from(source.getClasspathResourceName(), getPickleLocation(pickleEvent));
        }

        @Override
        UniqueId featureSegment(UniqueId parent, CucumberFeature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, feature.getUri().toString());
        }
    }

}
