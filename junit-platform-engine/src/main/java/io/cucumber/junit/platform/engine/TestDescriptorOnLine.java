package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.platform.engine.support.descriptor.FilePosition.fromQuery;

@SuppressWarnings("Convert2MethodRef")
class TestDescriptorOnLine {

    static Predicate<TestDescriptor> testDescriptorOnLine(int line) {
        return descriptor -> descriptor.getSource()
                .flatMap(testSource -> {
                    if (testSource instanceof FileSource) {
                        FileSource fileSystemSource = (FileSource) testSource;
                        return fileSystemSource.getPosition();
                    }
                    if (testSource instanceof ClasspathResourceSource) {
                        ClasspathResourceSource classpathResourceSource = (ClasspathResourceSource) testSource;
                        return classpathResourceSource.getPosition();
                    }
                    return Optional.empty();
                })
                .map(filePosition -> filePosition.getLine())
                .map(testSourceLine -> line == testSourceLine)
                .orElse(false);
    }

    private static boolean anyTestDescriptor(TestDescriptor testDescriptor) {
        return true;
    }

    static Predicate<TestDescriptor> from(UriSelector selector) {
        String query = selector.getUri().getQuery();
        return fromQuery(query)
                .map(filePosition -> filePosition.getLine())
                .map(TestDescriptorOnLine::testDescriptorOnLine)
                .orElse(TestDescriptorOnLine::anyTestDescriptor);
    }

    static Predicate<TestDescriptor> from(ClasspathResourceSelector selector) {
        return selector.getPosition()
                .map(filePosition -> filePosition.getLine())
                .map(TestDescriptorOnLine::testDescriptorOnLine)
                .orElse(TestDescriptorOnLine::anyTestDescriptor);
    }

    static Predicate<TestDescriptor> from(FileSelector selector) {
        return selector.getPosition()
                .map(filePosition -> filePosition.getLine())
                .map(TestDescriptorOnLine::testDescriptorOnLine)
                .orElse(TestDescriptorOnLine::anyTestDescriptor);
    }

}
