package io.cucumber.junit.platform.engine;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.descriptor.UriSource;

import java.net.URI;

import static org.junit.platform.engine.support.descriptor.ClasspathResourceSource.CLASSPATH_SCHEME;

abstract class FeatureSource {

    private static FilePosition createFilePosition(Location location) {
        return FilePosition.from(location.getLine(), location.getColumn());
    }

    static FeatureSource of(URI uri) {
        if (CLASSPATH_SCHEME.equals(uri.getScheme())) {
            ClasspathResourceSource source = ClasspathResourceSource.from(uri);
            return new FeatureClasspathSource(source);
        }
        UriSource source = UriSource.from(uri);
        if (source instanceof FileSource) {
            return new FeatureFileSource((FileSource) source);
        }
        return new FeatureUriSource(source);
    }

    abstract TestSource nodeSource(Node node);

    abstract TestSource source();

    private static class FeatureFileSource extends FeatureSource {

        private final FileSource source;

        FeatureFileSource(FileSource source) {
            this.source = source;
        }

        @Override
        TestSource nodeSource(Node node) {
            return source.withPosition(createFilePosition(node.getLocation()));
        }

        @Override
        TestSource source() {
            return source;
        }

    }

    private static class FeatureUriSource extends FeatureSource {

        private final UriSource source;

        FeatureUriSource(UriSource source) {
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

    private static class FeatureClasspathSource extends FeatureSource {

        private final ClasspathResourceSource source;

        FeatureClasspathSource(ClasspathResourceSource source) {
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
