package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.descriptor.UriSource;

import java.net.URI;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;

abstract class FeatureOrigin {

    private static final String RULE_SEGMENT_TYPE = "rule";
    private static final String FEATURE_SEGMENT_TYPE = "feature";
    private static final String SCENARIO_SEGMENT_TYPE = "scenario";
    private static final String EXAMPLES_SEGMENT_TYPE = "examples";
    private static final String EXAMPLE_SEGMENT_TYPE = "example";

    private static FilePosition createFilePosition(Location location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static FeatureOrigin fromUri(URI uri) {
        if (ClasspathResourceSource.CLASSPATH_SCHEME.equals(uri.getScheme())) {
            if (!uri.getSchemeSpecificPart().startsWith("/")) {
                // ClasspathResourceSource.from expects all resources to start
                // with a forward slash
                uri = URI.create(CLASSPATH_SCHEME_PREFIX + "/" + uri.getSchemeSpecificPart());
            }
            ClasspathResourceSource source = ClasspathResourceSource.from(uri);
            return new ClasspathFeatureOrigin(source);
        }

        UriSource source = UriSource.from(uri);
        if (source instanceof FileSource) {
            return new FileFeatureOrigin((FileSource) source);
        }

        return new UriFeatureOrigin(source);

    }

    static boolean isFeatureSegment(UniqueId.Segment segment) {
        return FEATURE_SEGMENT_TYPE.equals(segment.getType());
    }

    abstract TestSource featureSource();

    abstract TestSource nodeSource(Node node);

    abstract UniqueId featureSegment(UniqueId parent, Feature feature);

    UniqueId ruleSegment(UniqueId parent, Node rule) {
        return parent.append(RULE_SEGMENT_TYPE, String.valueOf(rule.getLocation().getLine()));
    }

    UniqueId scenarioSegment(UniqueId parent, Node scenarioDefinition) {
        return parent.append(SCENARIO_SEGMENT_TYPE, String.valueOf(scenarioDefinition.getLocation().getLine()));
    }

    UniqueId examplesSegment(UniqueId parent, Node examples) {
        return parent.append(EXAMPLES_SEGMENT_TYPE, String.valueOf(examples.getLocation().getLine()));
    }

    UniqueId exampleSegment(UniqueId parent, Node tableRow) {
        return parent.append(EXAMPLE_SEGMENT_TYPE, String.valueOf(tableRow.getLocation().getLine()));
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
        TestSource nodeSource(Node node) {
            return FileSource.from(source.getFile(), createFilePosition(node.getLocation()));
        }

        @Override
        UniqueId featureSegment(UniqueId parent, Feature feature) {
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
        TestSource nodeSource(Node node) {
            return source;
        }

        @Override
        UniqueId featureSegment(UniqueId parent, Feature feature) {
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
        TestSource nodeSource(Node node) {
            return ClasspathResourceSource.from(source.getClasspathResourceName(),
                createFilePosition(node.getLocation()));
        }

        @Override
        UniqueId featureSegment(UniqueId parent, Feature feature) {
            return parent.append(FEATURE_SEGMENT_TYPE, feature.getUri().toString());
        }

    }

}
