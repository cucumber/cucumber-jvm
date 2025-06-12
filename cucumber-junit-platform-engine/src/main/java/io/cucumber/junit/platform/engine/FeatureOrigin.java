package io.cucumber.junit.platform.engine;

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

    static final String RULE_SEGMENT_TYPE = "rule";
    static final String FEATURE_SEGMENT_TYPE = "feature";
    static final String SCENARIO_SEGMENT_TYPE = "scenario";
    static final String EXAMPLES_SEGMENT_TYPE = "examples";
    static final String EXAMPLE_SEGMENT_TYPE = "example";

    private static FilePosition createFilePosition(Location location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static FeatureOrigin fromUri(URI uri) {
        if (ClasspathResourceSource.CLASSPATH_SCHEME.equals(uri.getScheme())) {
            if (!uri.getSchemeSpecificPart().startsWith("/")) {
                // ClasspathResourceSource.from expects all resources to start
                // with a forward slash
                uri = URI.create(CLASSPATH_SCHEME_PREFIX + "/" + uri.getRawSchemeSpecificPart());
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

    abstract TestSource nodeSource(Node node);

    abstract TestSource source();

    private static class FileFeatureOrigin extends FeatureOrigin {

        private final FileSource source;

        FileFeatureOrigin(FileSource source) {
            this.source = source;
        }

        @Override
        TestSource nodeSource(Node node) {
            return FileSource.from(source.getFile(), createFilePosition(node.getLocation()));
        }

        @Override
        TestSource source() {
            return source;
        }

    }

    private static class UriFeatureOrigin extends FeatureOrigin {

        private final UriSource source;

        UriFeatureOrigin(UriSource source) {
            this.source = source;
        }

        @Override
        TestSource nodeSource(Node node) {
            return source;
        }

        @Override
        TestSource source() {
            return source;
        }
    }

    private static class ClasspathFeatureOrigin extends FeatureOrigin {

        private final ClasspathResourceSource source;

        ClasspathFeatureOrigin(ClasspathResourceSource source) {
            this.source = source;
        }

        @Override
        TestSource nodeSource(Node node) {
            return ClasspathResourceSource.from(source.getClasspathResourceName(),
                createFilePosition(node.getLocation()));
        }

        @Override
        TestSource source() {
            return source;
        }
    }

}
